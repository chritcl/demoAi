import * as THREE from 'three';
import { unref, type Ref } from 'vue'; // 引入 unref 用于解包 Ref
import type { GeoProjection } from 'd3-geo'; // 如果你安装了 @types/d3-geo
import markerPoint from '../assets/markerPoint.png';

export interface PointItem {
    id: string | number;
    type: string;
    lnglat: string; // '116.xxx,39.xxx'
    name?: string;
    [key: string]: any;
}

export interface PointConfig {
    baseHeight?: number;
    size?: number;
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

    // 获取纹理（带缓存）
    const getTexture = (url: string) => {
        if (textureCache.has(url)) return textureCache.get(url)!;
        const texture = textureLoader.load(url);
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
     * 创建点位组
     */
    const createPointsGroup = (dataList: PointItem[], config?: PointConfig) => {
        const group = new THREE.Group();
        group.name = 'MapPointsGroup';

        // 默认 Z 轴高度 (根据你的场景，如果是平面地图竖着放，这里就是 Z；如果是躺着放，可能是 Y)
        const baseHeight = config?.baseHeight || 0.16;
        const defaultSize = config?.size || 0.5;

        dataList.forEach(item => {
            if (!item.lnglat) return;

            const coords = item.lnglat.split(',');
            if (coords.length !== 2) return;

            // --- 核心修改点：调用转换函数 ---
            const position = transformPosition(coords[0], coords[1]);

            // 如果转换失败（比如不在投影范围内，或投影器未初始化），则跳过
            if (!position) return;

            const [x, y] = position;

            // 2. 获取样式配置
            const typeStyle = config?.typeConfig?.[item.type] || {};
            const iconUrl = typeStyle.icon || markerPoint;

            // 3. 创建材质
            const material = new THREE.SpriteMaterial({
                map: getTexture(iconUrl),
                color: typeStyle.color || 0xffffff,
                transparent: true,
                depthTest: true, // 建议开启，否则可能穿透模型
                depthWrite: false
            });

            // 4. 创建 Sprite
            const sprite = new THREE.Sprite(material);

            // 5. 设置位置 (使用转换后的 x, y)
            sprite.position.set(x, y, baseHeight);

            const size = typeStyle.size || defaultSize;
            sprite.scale.set(size, size, 1);

            // 6. 绑定业务数据
            sprite.userData = {
                ...item,
                isMapPoint: true
            };

            group.add(sprite);
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