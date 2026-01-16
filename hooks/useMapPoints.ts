import * as THREE from 'three';
import { unref, type Ref } from 'vue'; // 引入 unref 用于解包 Ref
import type { GeoProjection } from 'd3-geo'; // 如果你安装了 @types/d3-geo
import markerPoint from '../assets/markerPoint1.png';

export interface PointItem {
    id: string | number;
    type: string;
    lnglat: string; // '格式：116.xxx,39.xxx'
    name?: string;
    [key: string]: any;
}

export interface PointConfig {
    baseHeight?: number;
    size?: number;
    labelConfig?: { // 新增：文字配置
        show?: boolean;      // 是否显示文字
        color?: string;      // 文字颜色
        fontSize?: number;   // 字体大小 (canvas中的像素值，建议大一点以保证清晰度)
        scale?: number;      // 缩放比例 (控制场景中文字实际大小)
        offset?: [number, number]; // [x, y] 偏移量，相对于图标中心
    };
    typeConfig?: Record<string, {
        icon?: string;
        color?: string;
        size?: number;
    }>;
}

// 接收外部传入的 投影器Ref 和 地图尺寸Ref
export default function useMapPoints(
    projectionRef: Ref<GeoProjection | null>,
    mapSizeRef: Ref<number>
) {
    const textureLoader = new THREE.TextureLoader();
    const textureCache = new Map<string, THREE.Texture>();

    // -路径解析辅助函数 ---
    const getResolvedUrl = (url: string) => {
        if (!url) return markerPoint;

        // 1. 如果是网络图片 (http/https) 或 Base64 (data:)，直接返回
        if (url.startsWith('http') || url.startsWith('data:') || url.startsWith('//')) {
            return url;
        }

        // 2. 如果是 public 目录下的绝对路径 (例如 "/icons/car.png")，直接返回
        if (url.startsWith('/')) {
            return url;
        }

        // 3. 处理本地相对路径 (针对 Vite 环境)
        // 注意：Vite 要求这里的字符串不能完全动态，必须有明确的前缀或后缀让它能定位目录
        // 假设你的图片都在 src/assets 下，且传入的是 "myIcon.png" 或 "./myIcon.png"
        try {
            // 去掉开头的 ./ 或 ../assets/ 等路径，只保留文件名，统一处理
            const fileName = url.split('/').pop();

            // 使用 Vite 的 URL 解析 (注意：这里必须写死 assets 路径前缀，不能完全是变量)
            return new URL(`../assets/${fileName}`, import.meta.url).href;
        } catch (e) {
            console.error(`无法解析图片路径: ${url}`, e);
            return markerPoint;
        }
    };

    // 获取纹理（带缓存）
    const getTexture = (url: string) => {
        const resolvedUrl = getResolvedUrl(url); // 先解析路径
        if (textureCache.has(url)) return textureCache.get(url)!;
        const texture = textureLoader.load(resolvedUrl, (t) => {
            // 优化：纹理加载后自动更新，防止黑屏
            t.needsUpdate = true;
            t.colorSpace = THREE.SRGBColorSpace; //加入colorspace
        });
        textureCache.set(url, texture);
        return texture;
    };

    /**
     * 核心 helper：将经纬度转换为 Three.js 的平面坐标
     * @param lngStr 经度字符串
     * @param latStr 纬度字符串
     */
    const transformPosition = (lngStr: string, latStr: string): [number, number] | null => {
        // 1. 解包获取当前的投影器和尺寸
        const projection = unref(projectionRef);
        const mapSize = unref(mapSizeRef);

        if (!projection) {
            console.warn('Projection not ready yet');
            return null;
        }

        const lng = parseFloat(lngStr);
        const lat = parseFloat(latStr);

        // 2. 使用 D3 投影转换 (得到屏幕坐标 [x, y]，其中 y 是向下的)
        const projected = projection([lng, lat]);

        if (projected) {
            // 3. 执行与地图完全一致的 居中 和 Y轴反转
            const x = projected[0] - mapSize / 2;
            const y = (mapSize / 2) - projected[1]; // 反转 Y 轴，因为 3D 世界 Y 向上
            return [x, y];
        }
        return null;
    };

    /**
     * 创建文字 Sprite 的辅助函数
     * 原理：用 Canvas 画出文字，将其作为纹理贴在 Sprite 上
     */
    const createLabelSprite = (text: string, config: PointConfig['labelConfig']) => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        if (!ctx) return null;

        // 1. 配置参数
        const fontSize = config?.fontSize || 64; // 画布上的字体大小（大一点清晰）
        const fontColor = config?.color || '#ffffff';
        const fontFamily = 'Arial, "Microsoft YaHei", sans-serif';

        // 2. 测量文字宽度
        ctx.font = `bold ${fontSize}px ${fontFamily}`;
        const metrics = ctx.measureText(text);
        const textWidth = metrics.width;

        // 3. 设置画布大小 (加一点 padding 防止被切掉)
        // 注意：宽高的比例决定了 Sprite 的拉伸情况
        const padding = 10;
        canvas.width = textWidth + padding * 2;
        canvas.height = fontSize * 1.4; // 高度稍微多给一点

        // 4. 绘制背景 (可选：如果想要文字背景色，可以在这里 fillRect)
        // ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
        // ctx.fillRect(0, 0, canvas.width, canvas.height);

        // 5. 绘制文字
        ctx.font = `bold ${fontSize}px ${fontFamily}`;
        ctx.fillStyle = fontColor;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        // 绘制阴影以增强对比度 (可选)
        ctx.shadowColor = 'rgba(0,0,0,0.8)';
        ctx.shadowBlur = 4;
        ctx.shadowOffsetX = 2;
        ctx.shadowOffsetY = 2;

        ctx.fillText(text, canvas.width / 2, canvas.height / 2);

        // 6. 创建纹理
        const texture = new THREE.CanvasTexture(canvas);
        texture.minFilter = THREE.LinearFilter;
        texture.magFilter = THREE.LinearFilter; // 保证缩放不马赛克
        texture.needsUpdate = true;

        // 7. 创建 Sprite
        const material = new THREE.SpriteMaterial({
            map: texture,
            transparent: true,
            depthWrite: false, // 防止遮挡背景
            depthTest: true,
        });
        const sprite = new THREE.Sprite(material);

        // 8. 计算缩放比例
        // 我们希望文字在 3D 场景中的高度是固定的 (例如 0.5)，宽度根据宽高比自动算
        const labelScale = config?.scale || 0.3; // 基础缩放系数
        // Sprite 的 Y 轴高度 = labelScale
        // Sprite 的 X 轴宽度 = labelScale * (Canvas宽 / Canvas高)
        const aspectRatio = canvas.width / canvas.height;
        sprite.scale.set(labelScale * aspectRatio, labelScale, 1);

        return sprite;
    };

    /**
     * 创建点位组
     */
    const createPointsGroup = (dataList: PointItem[], config?: PointConfig) => {
        const group = new THREE.Group();
        group.name = 'MapPointsGroup';

        // 默认 Z 轴高度 (根据你的场景，如果是平面地图竖着放，这里就是 Z；如果是躺着放，可能是 Y)
        const baseHeight = config?.baseHeight || 0.16;
        const defaultSize = config?.size || 0.5;

        // 默认文字配置
        const labelCfg = {
            show: true,
            color: '#ffffff',
            fontSize: 64, // 内部渲染分辨率
            scale: 0.3,   // 场景显示大小
            offset: [0, -0.4], // 默认显示在图标下方
            ...config?.labelConfig
        };

        dataList.forEach(item => {
            if (!item.lnglat) return;

            const coords = item.lnglat.split(',');
            if (coords.length !== 2) return;

            // --- 调用转换函数 ---
            const position = transformPosition(coords[0], coords[1]);

            // 如果转换失败（比如不在投影范围内，或投影器未初始化），则跳过
            if (!position) return;

            const [x, y] = position;

            // 1. 创建图标Sprite
            const typeStyle = config?.typeConfig?.[item.type] || {};
            const iconUrl = typeStyle.icon;

            // 2. 创建材质
            const material = new THREE.SpriteMaterial({
                map: getTexture(iconUrl || ''),
                color: typeStyle.color || 0xffffff,
                transparent: true,
                depthWrite: false, // 必须关，解决背景穿透 BUG
                depthTest: true,   // 保持开启，维持正常的 3D 遮挡关系
            });

            // 3. 创建 Sprite
            const sprite = new THREE.Sprite(material);

            // 4. 设置位置 (使用转换后的 x, y)
            sprite.position.set(x, y, baseHeight);
            sprite.renderOrder = 10;

            const size = typeStyle.size || defaultSize;
            sprite.scale.set(size, size, 1);

            // 5. 绑定业务数据
            sprite.userData = {
                ...item,
                isMapPoint: true,
                originZ: baseHeight // 记录初始高度，用于悬停复位
            };

            group.add(sprite);

            // 创建文字 Sprite(如果配置开启且有名字)-- -
            if (labelCfg.show && item.name) {
                // @ts-ignore
                const labelSprite = createLabelSprite(item.name, labelCfg);
                if (labelSprite) {
                    // 计算文字位置：基于图标位置 + 偏移量
                    const offsetX = labelCfg.offset ? labelCfg.offset[0] : 0;
                    const offsetY = labelCfg.offset ? labelCfg.offset[1] : 0;

                    labelSprite.position.set(
                        x + offsetX,
                        y + offsetY,
                        baseHeight // 文字和图标在同一高度，或者稍微高一点 baseHeight + 0.01
                    );

                    labelSprite.renderOrder = 11; // 确保文字渲染在图标之上（如果有重叠）
                    // 也可以把业务数据绑给文字，这样点文字也能触发事件
                    labelSprite.userData = {
                        ...item, isMapPointLabel: true, originZ: baseHeight
                    };

                    group.add(labelSprite);
                }
            }
        });

        return group;
    };

    const disposePoints = () => {
        textureCache.forEach(t => t.dispose());
        textureCache.clear();
    };

    return {
        createPointsGroup,
        disposePoints
    };
}