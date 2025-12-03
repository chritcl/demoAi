<template>
    <div class="map-container">
        <!-- 3D地图容器 -->
        <div id="app-map" ref="mapContainerRef" class="is-full"></div>

        <transition name="fade">
            <div v-if="isLoading" class="loading-mask">
                <div class="loading-content">
                    <div class="spinner"></div>
                    <div class="loading-text">资源加载中 {{ loadProgress }}%</div>
                </div>
            </div>
        </transition>

        <div v-if="errorMessage" class="error-tip">
            渲染失败: {{ errorMessage }}
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * ThreeMap.vue - 3D地图可视化组件
 * 
 * 功能说明：
 * 1. 基于Three.js构建的3D地图可视化组件
 * 2. 从GeoJSON数据生成3D地图模型
 * 3. 支持地图标记点（光柱）、标签、边框线等元素
 * 4. 包含粒子效果、旋转装饰元素等动画效果
 * 5. 支持相机控制和交互操作
 */

// ==================== 导入依赖 ====================
import BaseEarth from './util/earth'; // 基础地球类，提供场景、相机、渲染器等基础功能
import TWEEN from '@tweenjs/tween.js'; // 补间动画库，用于平滑动画过渡
import * as THREE from 'three'; // Three.js核心库
import { GUI } from 'three/examples/jsm/libs/lil-gui.module.min.js'; // GUI调试面板
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue';
import { random, initCameraWithPitch } from './util/common'; // 随机数工具函数和GeoJSON中心点计算
import useCountry from './hooks/useCountry'; // 地图边界线创建Hook
import useCoord from './hooks/useCoord'; // 坐标工具Hook
import useConversionStandardData from './hooks/useConversionStandardData'; // 数据转换Hook
import useMapMarkedLightPillar from './hooks/useMapMarkedLightPillar'; // 光柱标记点Hook
import useSequenceFrameAnimate from './hooks/useSequenceFrameAnimate'; // 序列帧动画Hook
import useCSS2DRender from './hooks/useCSS2DRender'; // CSS2D标签渲染Hook

// ==================== 导入资源文件 ====================
import provinceJson from './data/430700_full.json'; // 省份GeoJSON数据
import mapBg from './assets/map-bg.jpg'; // 地图顶部纹理
import down from './assets/down.png'; // 地图底部纹理
import circle1 from './assets/circle1.png'; // 旋转光圈纹理
import circle2 from './assets/circle2.png'; // 旋转点纹理
import circlePointImg from './assets/circle-point.png'; // 中心点标记纹理
import sceneBgImg from './assets/scene-bg2.png'; // 场景背景纹理
import risingParticlesImg from './assets/risingParticles.png'; // 上升粒子序列帧纹理

// ==================== Props & Emits ====================
const props = defineProps<{
    mapData?: any; // 接收父组件传入的 GeoJSON 数据
    mapConfig?: {
        // 顶部材质数据
        topFaceMaterialData?: {
            // 顶部材质颜色 示例：#b4eeea
            color: string;
            // 顶部材质透明度 示例：true
            transparent: boolean;
            // 顶部材质透明度 示例：1
            opacity: number;
        };
        // 侧面材质数据
        sideFaceMaterialData?: {
            // 侧面材质颜色 示例：#123024
            color: string;
            // 侧面材质透明度 示例：true
            transparent: boolean;
            // 侧面材质透明度 示例：0.9
            opacity: number;
        };
        // 纹理重复缩放比例数据 示例：0.1
        scale?: number;
        // 区块高度 示例：0.15
        blockHeight?: number;
        // 中心底部光圈1
        circle1Data?: {
            // 光圈1透明度 示例：1
            opacity: number;
            // 光圈1宽度（根据地图宽度自动计算+-宽度值） 示例：1
            width: number;
            // 光圈1旋转速度 示例：0.0005 顺时针旋转
            speed: number;
            // 光圈1高度（基于平面） 示例：0
            height: number;
        }
        // 中心底部光圈2
        circle2Data?: {
            // 光圈2透明度 示例：1
            opacity: number;
            // 光圈2宽度（根据地图宽度自动计算+-宽度值） 示例：1
            width: number;
            // 光圈2旋转速度 示例：0.0005 逆时针旋转
            speed: number;
            // 光圈2高度（基于平面） 示例：0.0001
            height: number;
        }
        // 场景背景
        sceneBgData?: {
            // 场景背景颜色 示例：#ffffff
            color: string;
            // 场景背景透明度 示例：true
            transparent: boolean;
            // 场景背景透明度 示例：1
            opacity: number;
            // 背景宽度倍率（根据地图宽度自动计算*倍率） 示例：4
            widthRatio: number;
        }
        // 背景中心点点阵图
        circlePointData?: {
            // 背景中心点点阵图颜色 示例：#00ffff
            color: string;
            // 背景中心点点阵图透明度 示例：true
            transparent: boolean;
            // 背景中心点点阵图透明度 示例：1
            opacity: number;
            // 背景中心点点阵图宽度(根据地图宽度自动计算+-宽度值) 示例：1
            width: number;
        }
        // 粒子动画效果数据
        particleData?: {
            // 单帧宽度 示例：100
            width: number;
            // 单帧高度 示例：100
            height: number;
            // 总帧数（不建议修改） 示例：9
            frame: number;
            // 列数（不建议修改） 示例：9
            column: number;
            // 行数（不建议修改） 示例：1
            row: number;
            // 动画速度 示例：0.8
            speed: number;
        }
        // 边界线数据
        borderLineData?: {
            // 边界线颜色 示例：#ffffff
            topColor: string;
            // 边界线宽度 示例：2
            topLinewidth: number;
            // 边界线透明度 示例：true
            topTransparent: boolean;
            // 边界线颜色 示例：'#61fbfd'
            bottomColor: string;
            // 边界线宽度 示例：2
            bottomLinewidth: number;
        }
        // 标记点相关光柱数据
        lightPointBeamData?: {
            // 光柱长度倍率（值越大越小） 示例：8
            heightRatio: number;
            // 光柱高度 示例：0.01
            height: number;
        }
        // 区块标签
        blockLabelData?: {
            // 标签高度 示例：0.02
            height: number;
            // 标签字体大小 示例：40
            fontSize: number;
            // 标签字体颜色 示例：#ffffff
            fontColor: string;
            // 标签缩放比例 示例：0.1
            scale: number;
            // 左下偏移 示例：0
            xOffset: number;
            // 右下偏移 示例：0.08
            yOffset: number;
        }
        otherConfig?: {
            // 最大俯仰角角度 示例：60
            maxAngle: number;
            // 初始视野高度 示例：3越大视角越远
            distance: number;
            // 初始视野角度（俯仰角） 示例：45°
            fov: number;
            // 最大缩放距离 示例：10
            maxDistance: number;
            // 最小缩放距离 示例：2
            minDistance: number;
            // 缩放灵敏度 示例：0.3
            zoomSensitivity: number;
        }
        // 悬停相关数据
        hoverData?: {
            // 顶部材质颜色 示例：#00ffff
            topHoverColor: string;
            // 顶部材质透明度 示例：1
            topHoverOpacity: number;
            // 顶部材质自发光 示例：#00ffff
            topHoverEmissive: string;
            // 顶部材质自发光强度 示例：0.3
            topHoverEmissiveIntensity: number;
            // 侧面材质颜色 示例：#00ffff
            sideHoverColor: string;
            // 侧面材质透明度 示例：1
            sideHoverOpacity: number;
            // 侧面材质自发光 示例：#00ffff
            sideHoverEmissive: string;
            // 侧面材质自发光强度 示例：0.3
            sideHoverEmissiveIntensity: number;
            // 悬停动画时长(上浮) 示例：300（毫秒）
            hoverUpAnimationDuration: number;
            // 悬停动画时长(下沉) 示例：300（毫秒）
            hoverDownAnimationDuration: number;
            // 悬停高度 示例：0.05
            hoverHeight: number;
        }
        // 环境光
        ambientLightData?: {
            // 平行光1颜色 示例：#ffffff
            color1: string;
            // 平行光1角度 示例：30
            angle1: number;
            // 平行光1强度（从地图中心上方照射） 示例：4
            intensity1: number;
            // 平行光2颜色 示例：#ffffff
            color2: string;
            // 平行光2角度 示例：40
            angle2: number;
            // 平行光2强度（从地图中心上方照射，增强光照效果） 示例：2
            intensity2: number;
            // 环境光颜色 示例：#ffffff
            color3: string;
            // 环境光强度（提供整体基础照明） 示例：0.8
            intensity3: number;
        }
    }
}>();

const emit = defineEmits([
    'area-click',   //区块点击事件
    'load-start',      // 开始加载
    'load-progress',   // 加载进度 (0-100)
    'load-finish',     // 加载/渲染完成
    'render-error'     // 渲染错误
]); // 定义点击事件

// ==================== 状态管理 ====================
const mapContainerRef = ref<HTMLElement | null>(null);
const isLoading = ref(true);
const loadProgress = ref(0);
const errorMessage = ref('');

// 使用 shallowRef 存储 Three 实例，避免 Vue 深度代理造成性能损耗
const baseEarthRef = shallowRef<CurrentEarth | null>(null);
/** 鼠标位置 */
let mouse: THREE.Vector2 = new THREE.Vector2();

const handleResize = () => {
    if (baseEarthRef.value) {
        baseEarthRef.value.resize();
    }
};

// ResizeObserver 用于监听容器大小变化
let resizeObserver: ResizeObserver | null = null;

// ==================== 初始化Hooks ====================
/** GeoJSON数据转换工具 */
const { transfromGeoJSON } = useConversionStandardData();
/** 坐标工具，用于获取边界框等 */
const { getBoundingBox } = useCoord();
/** 创建地图边界线 */
const { createCountryFlatLine } = useCountry();
/** 标签渲染器，用于创建和渲染标签（使用Sprite材质，可贴在区块面上） */
const { createSpriteTag } = useCSS2DRender();
/** 创建光柱标记点，scaleFactor控制光柱缩放比例 */
const { createLightPillar } = useMapMarkedLightPillar({ scaleFactor: 1, });
/** 序列帧动画工具，用于创建粒子等动画效果 */
const { createSequenceFrame } = useSequenceFrameAnimate();

// ==================== 类定义  ====================
/**
 * CurrentEarth类 - 当前地图场景类
 * 继承自BaseEarth，实现具体的3D地图场景
 */
class CurrentEarth extends BaseEarth {
    /** 地图组对象，包含所有地图相关的3D对象 */
    mapGroup?: THREE.Group;
    /** 旋转光圈网格对象 */
    rotatingApertureMesh?: THREE.Mesh;
    /** 旋转点网格对象 */
    rotatingPointMesh?: THREE.Mesh;
    /** 粒子对象数组 */
    particleArr: THREE.Object3D[] = [];
    /** 区块原始材质备份 */
    originalMaterials: Map<THREE.Mesh, THREE.Material[]> = new Map();

    // 纹理相关
    textureLoader: THREE.TextureLoader | null = null;
    textures: Record<string, THREE.Texture> = {};

    // 材质相关
    topFaceMaterial: THREE.MeshPhongMaterial = new THREE.MeshPhongMaterial();
    sideMaterial: THREE.MeshLambertMaterial = new THREE.MeshLambertMaterial();
    /** 高亮材质 - 顶部 */
    hoverMaterialTop: THREE.MeshPhongMaterial;
    /** 高亮材质 - 侧面 */
    hoverMaterialSide: THREE.MeshPhongMaterial;

    // 状态
    centerXY: [number, number] = [111.705096, 29.014803];
    hoveredProvince: THREE.Object3D | null = null;  //当前悬停的区块对象
    provinceMap: Map<THREE.Object3D, any> = new Map();  //区块对象与属性数据的映射

    /** 平行光1 */
    directionalLight1?: THREE.DirectionalLight;
    /** 平行光2 */
    directionalLight2?: THREE.DirectionalLight;
    /** 环境光 */
    ambientLight?: THREE.AmbientLight;

    /** 射线投射器，用于鼠标拾取 */
    raycaster: THREE.Raycaster = new THREE.Raycaster();

    constructor(options: any, onProgress: (url: string, loaded: number, total: number) => void, onLoad: () => void) {
        super(options);

        // 1. 初始化 LoadingManager
        const manager = new THREE.LoadingManager();
        manager.onProgress = (url, itemsLoaded, itemsTotal) => {
            const progress = Math.floor((itemsLoaded / itemsTotal) * 100);
            onProgress(url, progress, itemsTotal);
        };
        manager.onLoad = () => {
            onLoad();
        };
        manager.onError = (url) => {
            console.error('There was an error loading ' + url);
            emit('render-error', `资源加载失败: ${url}`);
        };

        this.textureLoader = new THREE.TextureLoader(manager);
        this.initTextures(); // 立即开始加载纹理

        // 创建高亮材质（顶部带纹理）
        this.hoverMaterialTop = new THREE.MeshPhongMaterial({
            map: this.textures.mapBg, // 使用纹理贴图
            color: props.mapConfig?.hoverData?.topHoverColor || '#00ffff', // 青色高亮
            combine: THREE.MultiplyOperation, // 纹理与颜色相乘混合
            transparent: true,
            opacity: props.mapConfig?.hoverData?.topHoverOpacity || 1,
            emissive: props.mapConfig?.hoverData?.topHoverEmissive || '#00ffff', // 自发光
            emissiveIntensity: props.mapConfig?.hoverData?.topHoverEmissiveIntensity || 0.3,
        });
        // 创建高亮材质（侧面）
        this.hoverMaterialSide = new THREE.MeshPhongMaterial({
            color: props.mapConfig?.hoverData?.sideHoverColor || '#00ffff', // 青色高亮
            transparent: true,
            opacity: props.mapConfig?.hoverData?.sideHoverOpacity || 0.9,
            emissive: props.mapConfig?.hoverData?.sideHoverEmissive || '#00ffff', // 自发光
            emissiveIntensity: props.mapConfig?.hoverData?.sideHoverEmissiveIntensity || 0.3,
        });
    }

    /** 初始化纹理资源 */
    initTextures() {

        if (!this.textureLoader) return;

        // 预加载所有纹理
        this.textures = {
            /** 地图顶部纹理 */
            mapBg: this.textureLoader.load(mapBg),
            /** 地图底部纹理 */
            down: this.textureLoader.load(down),
            /** 旋转光圈纹理 */
            circle1: this.textureLoader.load(circle1),
            /** 旋转点纹理 */
            circle2: this.textureLoader.load(circle2),
            /** 中心点标记纹理 */
            circlePoint: this.textureLoader.load(circlePointImg),
            /** 场景背景纹理 */
            sceneBg: this.textureLoader.load(sceneBgImg),
        };

        // 配置纹理重复和翻转
        this.textures.mapBg.wrapS = this.textures.down.wrapS = THREE.RepeatWrapping; // 水平方向重复
        this.textures.mapBg.wrapT = this.textures.down.wrapT = THREE.RepeatWrapping; // 垂直方向重复
        this.textures.mapBg.flipY = this.textures.down.flipY = false; // 不翻转Y轴
        this.textures.mapBg.rotation = this.textures.down.rotation = THREE.MathUtils.degToRad(0);
        const scaleData = props.mapConfig?.scale || 0.6; // 纹理重复缩放比例
        this.textures.mapBg.repeat.set(scaleData, scaleData);
        this.textures.down.repeat.set(scaleData, scaleData);
    }

    /** 初始化材质 (需在纹理加载开始后调用) */
    initMaterials() {
        const config = props.mapConfig;

        this.topFaceMaterial = new THREE.MeshPhongMaterial({
            map: this.textures.mapBg,
            color: config?.topFaceMaterialData?.color || '#b4eeea',
            combine: THREE.MultiplyOperation,
            transparent: config?.topFaceMaterialData?.transparent ?? true,
            opacity: config?.topFaceMaterialData?.opacity ?? 1,
        });

        this.sideMaterial = new THREE.MeshLambertMaterial({
            color: config?.sideFaceMaterialData?.color || '#123024',
            transparent: config?.sideFaceMaterialData?.transparent ?? true,
            opacity: config?.sideFaceMaterialData?.opacity ?? 0.9,
        });

        // 悬停材质
        this.hoverMaterialTop = new THREE.MeshPhongMaterial({
            map: this.textures.mapBg,
            color: config?.hoverData?.topHoverColor || '#00ffff',
            combine: THREE.MultiplyOperation,
            transparent: true,
            opacity: config?.hoverData?.topHoverOpacity ?? 1,
            emissive: config?.hoverData?.topHoverEmissive || '#00ffff',
            emissiveIntensity: config?.hoverData?.topHoverEmissiveIntensity ?? 0.3,
        });

        this.hoverMaterialSide = new THREE.MeshPhongMaterial({
            color: config?.hoverData?.sideHoverColor || '#00ffff',
            transparent: true,
            opacity: config?.hoverData?.sideHoverOpacity ?? 0.9,
            emissive: config?.hoverData?.sideHoverEmissive || '#00ffff',
            emissiveIntensity: config?.hoverData?.sideHoverEmissiveIntensity ?? 0.3,
        });
    }

    /**
     * 初始化相机
     * 设置透视相机的位置、朝向和参数
     */
    initCamera() {
        const { width, height } = this.options;
        this.camera = new THREE.PerspectiveCamera(30, width / height, 0.1, 2000000);
        this.camera.up.set(0, 0, 1);
        this.camera.position.set(111.705096, 29.014803, 5.274838);
    }

    /**
     * 清理模型和内存
     */
    clearModel() {
        // 清理地图组
        if (this.mapGroup) {
            // 递归遍历清理几何体和材质
            this.mapGroup.traverse((obj: any) => {
                if (obj.isMesh) {
                    if (obj.geometry) obj.geometry.dispose();
                    if (Array.isArray(obj.material)) {
                        obj.material.forEach((m: any) => m.dispose());
                    } else if (obj.material) {
                        obj.material.dispose();
                    }
                }
            });
            this.scene.remove(this.mapGroup);
            this.mapGroup = undefined;
        }

        // 清理引用
        this.rotatingApertureMesh = undefined;
        this.rotatingPointMesh = undefined;
        this.particleArr = [];

        // 清空映射
        this.provinceMap.clear();
        this.originalMaterials.clear();
        this.hoveredProvince = null;
    }

    /**
     * 清理光源
     */
    clearLights() {
        if (this.directionalLight1) {
            this.removeObject(this.directionalLight1);
            this.directionalLight1 = undefined;
        }
        if (this.directionalLight2) {
            this.removeObject(this.directionalLight2);
            this.directionalLight2 = undefined;
        }
        if (this.ambientLight) {
            this.removeObject(this.ambientLight);
            this.ambientLight = undefined;
        }
    }

    /**
     * 更新地图
     */
    async updateMap(data: any) {
        if (!data) return;
        this.clearModel();
        // 清理旧的光源，避免叠加
        this.clearLights();

        // 确保材质是最新的
        this.initMaterials();

        // 更新相关配置
        const scaleData = props.mapConfig?.scale || 0.6; // 纹理重复缩放比例
        this.textures.mapBg.repeat.set(scaleData, scaleData);
        this.textures.down.repeat.set(scaleData, scaleData);
        this.topFaceMaterial = new THREE.MeshPhongMaterial({
            map: this.textures.mapBg,
            color: props.mapConfig?.topFaceMaterialData?.color || '#b4eeea',
            combine: THREE.MultiplyOperation, // 纹理与颜色相乘混合
            transparent: props.mapConfig?.topFaceMaterialData?.transparent || true,
            opacity: props.mapConfig?.topFaceMaterialData?.opacity || 1,
        });
        this.sideMaterial = new THREE.MeshLambertMaterial({
            color: props.mapConfig?.sideFaceMaterialData?.color || '#123024',
            transparent: props.mapConfig?.sideFaceMaterialData?.transparent || true,
            opacity: props.mapConfig?.sideFaceMaterialData?.opacity || 0.9,
        });

        // 重新初始化模型（会重新创建光源和控制器）
        await this.initModel(data);

        // 模型初始化后，根据计算出的中心点更新相机位置
        if (this.camera && this.controls) {
            // 使用 initModel 中计算出的中心点
            initCameraWithPitch(this.camera, { x: this.centerXY[0], y: this.centerXY[1] }, props.mapConfig?.otherConfig?.distance || 3, props.mapConfig?.otherConfig?.fov || 45);
            this.controls.target.set(this.centerXY[0], this.centerXY[1], 0);
            this.controls.update();
        }
    }


    /**
     * 初始化3D模型
     * 从GeoJSON数据创建3D地图模型
     */
    async initModel(data?: any) {
        if (!data) return;
        try {
            // 创建地图组，用于管理所有地图相关的3D对象
            this.mapGroup = new THREE.Group();

            // 遍历GeoJSON的每个要素（feature）
            data.features.forEach((elem: any) => {
                // 为每个要素创建一个对象容器
                const province = new THREE.Object3D();
                // 获取坐标数据（可能是MultiPolygon或Polygon）
                const coordinates = elem.geometry.coordinates;
                // 获取属性数据（包含名称、中心点等信息）
                const properties = elem.properties;

                // 标记区块对象，用于交互识别
                province.userData.isProvince = true;
                province.userData.properties = properties;

                // 保存区块与属性的映射关系
                this.provinceMap.set(province, properties);

                // 处理MultiPolygon坐标（多个多边形）
                coordinates.forEach((multiPolygon: any) => {
                    // 处理每个多边形
                    multiPolygon.forEach((polygon: any) => {
                        // 创建Shape对象用于绘制2D形状
                        const shape = new THREE.Shape();
                        // 将坐标点转换为Shape路径
                        for (let i = 0; i < polygon.length; i++) {
                            let [x, y] = polygon[i];
                            if (i === 0) {
                                shape.moveTo(x, y); // 移动到起始点
                            }
                            else shape.lineTo(x, y); // 绘制到下一个点
                        }
                        // 拉伸设置：将2D形状拉伸成3D几何体
                        const extrudeSettings = {
                            depth: props.mapConfig?.blockHeight || 0.15,           // 拉伸深度
                            bevelEnabled: false,   // 启用倒角
                        };
                        // 创建拉伸几何体
                        const geometry = new THREE.ExtrudeGeometry(shape, extrudeSettings);
                        // 创建网格（使用顶部和侧面材质）
                        const mesh = new THREE.Mesh(geometry, [this.topFaceMaterial, this.sideMaterial]);

                        // 备份原始材质
                        this.originalMaterials.set(mesh, [this.topFaceMaterial, this.sideMaterial]);

                        province.add(mesh);
                    });
                });

                // 生成光柱、标签、边线 (注意传递父对象)
                this.initExtraElements(properties, province, elem);

                // 将省份对象添加到地图组
                if (this.mapGroup) {
                    this.mapGroup.add(province);
                }
            });
            // 装饰元素初始化...
            this.initDecorations();

            // 将地图组添加到场景中
            this.scene.add(this.mapGroup);

            this.initLight();
            this.initControls();

        } catch (error) {
            console.log(error);
        }
    }

    // 光柱标记点、文字标签、边线
    initExtraElements(properties: any, province: THREE.Object3D, elem: any) {
        // 为每个要素创建光柱标记点（添加到区块对象上，使光柱跟随区块）
        this.initLightPoint(properties, province);
        // 为每个要素创建文字标签（添加到区块对象上，使标签跟随区块）
        this.initLabel(properties, province);

        // 为每个要素创建边界线（添加到区块对象上，使边界线跟随区块上浮）
        const featureData = {
            type: 'FeatureCollection',
            features: [elem]
        };
        this.initBorderLine(featureData, province, this.renderer || undefined);
    }

    initDecorations() {
        if (!this.mapGroup) return;
        // 获取地图组的边界框，用于计算中心点和尺寸
        let earthGroupBound = getBoundingBox(this.mapGroup);
        // 更新地图中心点坐标
        this.centerXY = [earthGroupBound.center.x, earthGroupBound.center.y];
        let { size } = earthGroupBound;
        // 计算装饰元素的宽度（取较大的尺寸并加1）
        let width = size.x < size.y ? size.y + 1 : size.x + 1;

        const circle1Width = props.mapConfig?.circle1Data?.width ? (width + props.mapConfig?.circle1Data?.width) : width;
        const circle2Width = props.mapConfig?.circle2Data?.width ? (width + props.mapConfig?.circle2Data?.width) : width + 1.5;
        const circlePointWidth = props.mapConfig?.circlePointData?.width ? (width + props.mapConfig?.circlePointData?.width) : width;

        // 添加装饰元素 - 注意这里改为添加到 mapGroup
        this.rotatingApertureMesh = this.initRotatingAperture(this.mapGroup, circle1Width);
        this.rotatingPointMesh = this.initRotatingPoint(this.mapGroup, circle2Width);
        this.initCirclePoint(this.mapGroup, circlePointWidth);
        this.initSceneBg(this.mapGroup, width);
        this.particleArr = this.initParticle(this.mapGroup, earthGroupBound);
    }

    /**
     * 初始化旋转光圈
     * 在地图中心位置创建一个旋转的光圈装饰效果
     * @param parent - 父对象
     * @param width - 光圈宽度
     * @returns 返回创建的网格对象，用于后续动画控制
     */
    initRotatingAperture(parent: THREE.Object3D, width: number) {
        let plane = new THREE.PlaneGeometry(width, width);
        let material = new THREE.MeshBasicMaterial({
            map: this.textures.circle1,
            transparent: true,
            opacity: props.mapConfig?.circle1Data?.opacity || 1,
            depthTest: true,
        });
        let mesh = new THREE.Mesh(plane, material);
        mesh.position.set(...this.centerXY, props.mapConfig?.circle1Data?.height || 0);
        mesh.scale.set(1.1, 1.1, 1.1);
        parent.add(mesh);
        return mesh;
    };

    /**
     * 初始化旋转点
     * 在地图底部创建一个旋转的点装饰效果
     * @param parent - 父对象
     * @param width - 旋转点宽度
     * @returns 返回创建的网格对象，用于后续动画控制
     */
    initRotatingPoint(parent: THREE.Object3D, width: number) {
        let plane = new THREE.PlaneGeometry(width, width);
        let material = new THREE.MeshBasicMaterial({
            map: this.textures.circle2,
            transparent: true,
            opacity: props.mapConfig?.circle2Data?.opacity || 1,
            depthTest: true,
        });
        let mesh = new THREE.Mesh(plane, material);
        mesh.position.set(...this.centerXY, props.mapConfig?.circle2Data?.height || 0.0001);
        mesh.scale.set(1.1, 1.1, 1.1);
        parent.add(mesh);
        return mesh;
    }

    /**
     * 初始化场景背景
     * 在地图底部创建一个大的背景平面
     * @param parent - 父对象
     * @param width - 背景宽度（实际为width * 4）
     */
    initSceneBg(parent: THREE.Object3D, width: number) {
        const widthRatio = props.mapConfig?.sceneBgData?.widthRatio || 4;
        let plane = new THREE.PlaneGeometry(width * widthRatio, width * widthRatio);
        let material = new THREE.MeshPhongMaterial({
            color: props.mapConfig?.sceneBgData?.color || '#ffffff',
            map: this.textures.sceneBg,
            transparent: props.mapConfig?.sceneBgData?.transparent || true,
            opacity: props.mapConfig?.sceneBgData?.opacity || 1,
            depthTest: true,
        });

        let mesh = new THREE.Mesh(plane, material);
        mesh.position.set(...this.centerXY, bottomZ - 0.2);
        parent.add(mesh);
    }

    /**
     * 初始化背景中心点点阵图
     * 在地图中心位置创建一个点阵图
     * @param parent - 父对象
     * @param width - 点阵图宽度
     */
    initCirclePoint(parent: THREE.Object3D, width: number) {
        let plane = new THREE.PlaneGeometry(width, width);
        let material = new THREE.MeshPhongMaterial({
            color: props.mapConfig?.circlePointData?.color || '#00ffff', // 青色
            map: this.textures.circlePoint,
            transparent: props.mapConfig?.circlePointData?.transparent || true,
            opacity: props.mapConfig?.circlePointData?.opacity || 1,
            depthTest: false,
        });
        let mesh = new THREE.Mesh(plane, material);
        mesh.position.set(...this.centerXY, bottomZ - 0.2);
        // let mesh2 = mesh.clone()
        // mesh2.position.set(...centerXY, bottomZ - 0.001)
        parent.add(mesh);
    }

    /**
     * 初始化粒子效果
     * 在地图周围创建多个上升的粒子动画效果
     * @param parent - 父对象
     * @param bound - 边界框信息，包含中心点和尺寸
     * @returns 返回创建的粒子对象数组，用于后续动画控制
     */
    initParticle(parent: THREE.Object3D, bound: { center: THREE.Vector3; size: THREE.Vector3 }) {
        // 获取中心点和地图大小
        let { center, size } = bound;
        // 构建粒子分布范围，基于地图尺寸
        let minX = center.x - size.x;
        let maxX = center.x + size.x;
        let minY = center.y - size.y;
        let maxY = center.y + size.y;
        let minZ = -6; // 粒子Z轴最小值
        let maxZ = 6;  // 粒子Z轴最大值

        let particleArr: THREE.Object3D[] = [];
        // 创建16个粒子
        for (let i = 0; i < 16; i++) {
            // 使用序列帧创建粒子动画
            const particle = createSequenceFrame({
                image: risingParticlesImg, // 粒子序列帧图片
                width: props.mapConfig?.particleData?.width || 100,   // 单帧宽度
                height: props.mapConfig?.particleData?.height || 100,  // 单帧高度
                frame: props.mapConfig?.particleData?.frame || 9,     // 总帧数
                column: props.mapConfig?.particleData?.column || 9,    // 列数
                row: props.mapConfig?.particleData?.row || 1,       // 行数
                speed: props.mapConfig?.particleData?.speed || 0.8,   // 动画速度
            });
            // 随机粒子缩放
            let particleScale = random(5, 10) / 1000;
            particle.scale.set(particleScale, particleScale, particleScale);
            particle.rotation.x = Math.PI / 2; // 旋转90度，使粒子面向相机
            // 随机位置
            let x = random(minX, maxX);
            let y = random(minY, maxY);
            let z = random(minZ, maxZ);
            particle.position.set(x, y, z);
            particleArr.push(particle);
        }
        parent.add(...particleArr);
        return particleArr;
    }

    /**
     * 创建地图顶部和底部边界线
     * 在地图的上方和下方创建边界线，增强视觉效果
     * @param data - GeoJSON数据
     * @param parent - 父对象（地图组或区块对象）
     * @param renderer - 渲染器对象（可选），用于获取分辨率
     */
    initBorderLine(data: any, parent: THREE.Object3D, renderer?: THREE.WebGLRenderer) {
        // 获取渲染器分辨率，用于Line2线宽计算
        let resolution: THREE.Vector2;
        if (renderer) {
            const size = new THREE.Vector2();
            renderer.getSize(size);
            resolution = size;
        } else {
            resolution = new THREE.Vector2(window.innerWidth, window.innerHeight);
        }

        // 创建顶部边界线（白色）
        let lineTop = createCountryFlatLine(
            data,
            {
                color: props.mapConfig?.borderLineData?.topColor || '#ffffff', // 白色
                linewidth: props.mapConfig?.borderLineData?.topLinewidth || 2, // 线宽，Line2支持更大的线宽
                transparent: props.mapConfig?.borderLineData?.topTransparent || true,
                depthTest: false, // 禁用深度测试，确保始终可见
            },
            'Line2', // 使用Line2类型，支持更粗的线条
            resolution
        );
        lineTop.position.z += props.mapConfig?.blockHeight || 0.15; // 将顶部线条提升到地图上方

        // 创建底部边界线（青色）
        let lineBottom = createCountryFlatLine(
            data,
            {
                color: props.mapConfig?.borderLineData?.bottomColor || '#61fbfd', // 青色
                linewidth: props.mapConfig?.borderLineData?.bottomLinewidth || 2,
                // transparent: true,
                depthTest: false,
            },
            'Line2',
            resolution
        );
        // lineBottom.position.z += 0.305; // 底部线条保持在地图底部
        // 添加边线到父对象
        parent.add(lineTop);
        parent.add(lineBottom);
    }

    /**
     * 创建光柱标记点
     * 在地图上的指定位置创建光柱效果，用于标记重要地点
     * @param properties - GeoJSON属性对象，包含centroid或center坐标
     * @param provinceObject - 区块对象，光柱将作为子对象添加到其上
     * @returns 成功创建返回true，否则返回false
     */
    initLightPoint(properties: any, provinceObject: THREE.Object3D) {
        // 检查是否有坐标信息
        if (!properties.centroid && !properties.center) {
            return false;
        }
        // 创建光柱，长度随机变化
        const heightRatio = props.mapConfig?.lightPointBeamData?.heightRatio || 8;
        let heightScaleFactor = 0.2 + random(1, 5) / heightRatio; // 长度缩放因子：0.4-1.2
        let lightCenter = properties.centroid || properties.center;
        let light = createLightPillar(lightCenter[0], lightCenter[1], heightScaleFactor);

        const blockHeight = props.mapConfig?.blockHeight || 0.15;
        const lightHeight = props.mapConfig?.lightPointBeamData?.height || 0.01;
        light.position.z = blockHeight + lightHeight; // 设置光柱Z轴位置

        // 标记为光柱对象
        light.userData.isLightPillar = true;

        // 将光柱添加到区块对象上，这样光柱会跟随区块的变换
        provinceObject.add(light);
        return true;
    }

    /**
     * 创建Sprite标签（贴在区块面上）
     * 使用Sprite材质创建标签，标签会始终面向相机，但位置会跟随区块
     * @param properties - GeoJSON属性对象，包含name和center坐标
     * @param provinceObject - 区块对象，标签将作为子对象添加到其上
     * @returns 成功创建返回true，否则返回false
     */
    initLabel(properties: any, provinceObject: THREE.Object3D) {
        // 检查是否有坐标信息和名称
        if ((!properties.centroid && !properties.center) || !properties.name) {
            return false;
        }

        // 获取标签位置
        let labelCenter = properties.centroid;

        const blockHeight = props.mapConfig?.blockHeight || 0.15;
        const labelHeight = props.mapConfig?.blockLabelData?.height || 0.02;

        const xOffset = props.mapConfig?.blockLabelData?.xOffset || 0;
        const yOffset = props.mapConfig?.blockLabelData?.yOffset || 0.08;

        // 使用 createSpriteTag 创建标签
        const sprite = createSpriteTag(
            properties.name,
            [labelCenter[0] + xOffset, labelCenter[1] + yOffset, blockHeight + labelHeight], // 设置在地图顶部上方
            {
                fontSize: props.mapConfig?.blockLabelData?.fontSize || 40,
                fontColor: props.mapConfig?.blockLabelData?.fontColor || '#ffffff',
                backgroundColor: 'transparent',
                // scale: props.mapConfig?.blockLabelData?.scale || 0.1,
            }
        );

        // 标记为标签对象，用于交互时排除
        sprite.userData.isLabel = true;

        // 将标签添加到区块对象上，这样标签会跟随区块的变换
        provinceObject.add(sprite);

        return true;
    }

    /**
     * 处理区块悬停效果
     * @param province - 悬停的区块对象
     */
    handleProvinceHover(province: THREE.Object3D) {
        if (this.hoveredProvince === province) return;

        // 恢复之前悬停区块的状态
        this.resetProvinceState();

        this.hoveredProvince = province;

        // 高亮效果：修改材质
        province.traverse((child) => {
            if (child instanceof THREE.Mesh && !child.userData.isLabel) {
                const originalMats = this.originalMaterials.get(child);
                if (originalMats && this.hoverMaterialTop && this.hoverMaterialSide) {
                    child.material = [this.hoverMaterialTop, this.hoverMaterialSide];
                }
            }
        });

        // 上浮效果：使用补间动画
        const targetZ = province.position.z + (props.mapConfig?.hoverData?.hoverHeight || 0.05);
        new TWEEN.Tween(province.position)
            .to({ z: targetZ }, props.mapConfig?.hoverData?.hoverUpAnimationDuration || 300)
            .easing(TWEEN.Easing.Quadratic.Out)
            .start();

        // 改变鼠标样式
        if (this.renderer) {
            this.renderer.domElement.style.cursor = 'pointer';
        }
    }

    /**
     * 重置区块状态
     */
    resetProvinceState() {
        if (!this.hoveredProvince) return;

        // 恢复材质
        this.hoveredProvince.traverse((child) => {
            if (child instanceof THREE.Mesh && !child.userData.isLabel) {
                const originalMats = this.originalMaterials.get(child);
                if (originalMats) {
                    child.material = originalMats;
                }
            }
        });

        // 恢复位置：使用补间动画
        new TWEEN.Tween(this.hoveredProvince.position)
            .to({ z: 0 }, props.mapConfig?.hoverData?.hoverDownAnimationDuration || 300)
            .easing(TWEEN.Easing.Quadratic.Out)
            .start();

        this.hoveredProvince = null;

        // 恢复鼠标样式
        if (this.renderer) {
            this.renderer.domElement.style.cursor = 'default';
        }
    }

    /**
     * 处理区块点击事件
     * @param province - 点击的区块对象
     */
    handleProvinceClick(province: THREE.Object3D) {
        const properties = this.provinceMap.get(province);
        if (properties) {
            console.log('点击区块信息:', properties);
            // 触发自定义事件
            emit('area-click', properties);
        }
    }
    /**
     * 初始化控制器
     * 设置轨道控制器的目标点为地图中心
     */
    initControls() {
        const result = super.initControls();
        if (this.controls) {
            // 设置控制器目标为地图中心点
            this.controls.target = new THREE.Vector3(...this.centerXY, 0);
            this.controls.minDistance = props.mapConfig?.otherConfig?.minDistance || 2;   // 最小缩放（相机离目标最近距离）
            this.controls.maxDistance = props.mapConfig?.otherConfig?.maxDistance || 10;  // 最大缩放（相机离目标最远距离）
            this.controls.zoomSpeed = props.mapConfig?.otherConfig?.zoomSensitivity || 0.3;   // 缩放灵敏度
        }
        return result;
    }

    updateControls() {
        if (this.controls) {
            // 设置控制器目标为地图中心点
            this.controls.target = new THREE.Vector3(...this.centerXY, 0);
            this.controls.minDistance = props.mapConfig?.otherConfig?.minDistance || 2;   // 最小缩放（相机离目标最近距离）
            this.controls.maxDistance = props.mapConfig?.otherConfig?.maxDistance || 10;  // 最大缩放（相机离目标最远距离）
            this.controls.zoomSpeed = props.mapConfig?.otherConfig?.zoomSensitivity || 0.3;   // 缩放灵敏度
        }
    }

    /**
     * 初始化光源
     * 创建平行光和环境光，为场景提供照明
     */
    initLight() {
        this.clearLights();
        // 平行光1：从地图中心上方照射
        let directionalLight1 = new THREE.DirectionalLight(props.mapConfig?.ambientLightData?.color1 || '#7af4ff', props.mapConfig?.ambientLightData?.intensity1 || 4);
        directionalLight1.position.set(...this.centerXY, props.mapConfig?.ambientLightData?.angle1 || 30);
        // 平行光2：从地图中心上方照射（增强光照效果）
        let directionalLight2 = new THREE.DirectionalLight(props.mapConfig?.ambientLightData?.color2 || '#7af4ff', props.mapConfig?.ambientLightData?.intensity2 || 2);
        directionalLight2.position.set(...this.centerXY, props.mapConfig?.ambientLightData?.angle2 || 40);
        // 环境光：提供整体基础照明
        let ambientLight = new THREE.AmbientLight(props.mapConfig?.ambientLightData?.color3 || '#7af4ff', props.mapConfig?.ambientLightData?.intensity3 || 0.8);
        // 将光源添加到场景中
        this.addObject(directionalLight1);
        this.addObject(directionalLight2);
        this.addObject(ambientLight);
        this.directionalLight1 = directionalLight1;
        this.directionalLight2 = directionalLight2;
        this.ambientLight = ambientLight;
    }


    /**
     * 初始化渲染器
     * 调用父类方法初始化WebGL渲染器
     */
    initRenderer() {
        super.initRenderer();
        // 如果需要sRGB编码，可以取消注释
        // this.renderer.outputEncoding = THREE.sRGBEncoding

        // 添加鼠标事件监听
        this.initMouseEvents();
    }

    /**
     * 初始化鼠标事件
     */
    initMouseEvents() {
        if (!this.renderer) return;

        const canvas = this.renderer.domElement;

        // 鼠标移动事件
        canvas.addEventListener('mousemove', (event: MouseEvent) => {
            // 计算标准化设备坐标 (-1 到 +1)
            const rect = canvas.getBoundingClientRect();
            mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
            mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

            // 更新射线
            if (this.camera) {
                this.raycaster.setFromCamera(mouse, this.camera);

                // 检测与地图组的交互
                if (this.mapGroup) {
                    const intersects = this.raycaster.intersectObjects(this.mapGroup.children, true);

                    if (intersects.length > 0) {
                        // 找到第一个区块对象（排除标签和光柱）
                        let targetProvince: THREE.Object3D | null = null;

                        for (const intersect of intersects) {
                            let obj = intersect.object;

                            // 向上查找直到找到区块对象
                            while (obj.parent && obj.parent !== this.mapGroup) {
                                obj = obj.parent;
                            }

                            // 检查是否是区块对象
                            if (obj.userData.isProvince) {
                                targetProvince = obj;
                                break;
                            }
                        }

                        if (targetProvince) {
                            this.handleProvinceHover(targetProvince);
                        } else {
                            this.resetProvinceState();
                        }
                    } else {
                        this.resetProvinceState();
                    }
                }
            }
        });

        // 鼠标点击事件
        canvas.addEventListener('click', (event: MouseEvent) => {
            // 计算标准化设备坐标
            const rect = canvas.getBoundingClientRect();
            mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
            mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

            // 更新射线
            if (this.camera) {
                this.raycaster.setFromCamera(mouse, this.camera);

                // 检测与地图组的交互
                if (this.mapGroup) {
                    const intersects = this.raycaster.intersectObjects(this.mapGroup.children, true);

                    if (intersects.length > 0) {
                        // 找到第一个区块对象
                        let targetProvince: THREE.Object3D | null = null;

                        for (const intersect of intersects) {
                            let obj = intersect.object;

                            // 向上查找直到找到区块对象
                            while (obj.parent && obj.parent !== this.mapGroup) {
                                obj = obj.parent;
                            }

                            // 检查是否是区块对象
                            if (obj.userData.isProvince) {
                                targetProvince = obj;
                                break;
                            }
                        }

                        if (targetProvince) {
                            this.handleProvinceClick(targetProvince);
                        }
                    }
                }
            }
        });
    }

    /**
     * 动画循环
     * 每一帧执行以下操作：
     * 1. 渲染场景
     * 2. 更新控制器
     * 3. 更新统计信息
     * 4. 更新旋转动画
     * 5. 渲染2D标签
     * 6. 更新粒子动画
     * 7. 更新补间动画
     */
    loop() {
        // 请求下一帧动画
        this.animationStop = window.requestAnimationFrame(() => {
            this.loop();
        });
        // 渲染3D场景
        if (this.renderer && this.camera) {
            this.renderer.render(this.scene, this.camera);
        }
        // 更新相机控制器（旋转、缩放等）
        if (this.options.controls.visibel && this.controls) {
            // this.controls.target.set(...centerXY, 0)
            this.controls.update();
        }
        // 旋转光圈动画（顺时针旋转）
        if (this.rotatingApertureMesh) {
            this.rotatingApertureMesh.rotation.z += props.mapConfig?.circle1Data?.speed || 0.0005;
        }
        // 旋转点动画（逆时针旋转）
        if (this.rotatingPointMesh) {
            this.rotatingPointMesh.rotation.z -= props.mapConfig?.circle2Data?.speed || 0.0005;
        }
        // 更新粒子动画：粒子上升效果
        if (this.particleArr.length) {
            for (let i = 0; i < this.particleArr.length; i++) {
                const particle = this.particleArr[i] as any;
                // 更新序列帧动画
                if (particle.updateSequenceFrame) {
                    particle.updateSequenceFrame();
                }
                // 粒子向上移动
                particle.position.z += 0.01;
                // 如果粒子超出上限，重置到底部（循环效果）
                if (particle.position.z >= 6) {
                    particle.position.z = -6;
                }
            }
        }
        // 更新补间动画
        TWEEN.update();
        // console.log(this.camera.position)
    }

    /**
     * 窗口大小改变时重置场景
     * 重新计算渲染器尺寸、相机参数和标签渲染器尺寸
     */
    resize() {
        super.resize();
        // 重新渲染场景并设置像素比
        if (this.renderer && this.camera) {
            this.renderer.render(this.scene, this.camera);
            this.renderer.setPixelRatio(window.devicePixelRatio);
        }
    }

    /**
     * 销毁
     */
    destroy() {

        // 2. 清理模型和光源
        this.clearModel();
        this.clearLights();

        // 3. 停止所有 TWEEN 动画 (很重要，否则报错)
        TWEEN.removeAll();

        // 4. 清理纹理缓存
        Object.values(this.textures).forEach(texture => texture.dispose());
        this.textures = {};

        // 5. 清理材质
        this.topFaceMaterial.dispose();
        this.sideMaterial.dispose();
        this.hoverMaterialTop.dispose();
        this.hoverMaterialSide.dispose();

        // 6. 调用基类销毁 (停止 RAF，销毁 WebGL 上下文)
        super.destroy();

        // 7. 清除引用
        this.mapGroup = undefined;
        this.provinceMap.clear();
        this.originalMaterials.clear();
    }
}

/** 地图底部Z轴位置 */
const bottomZ = -0.2;

// ==================== 组件挂载 ====================
/**
 * 组件挂载时初始化3D地图场景
 */
onMounted(async () => {
    if (!mapContainerRef.value) return;
    emit('load-start');
    isLoading.value = true;
    loadProgress.value = 0;

    try {

        const earthInstance = new CurrentEarth(
            {
                container: '#app-map', // 容器选择器
                axesVisibel: true,         // 显示坐标轴（调试用）
                controls: {
                    enableDamping: true,   // 启用阻尼效果，使相机移动更平滑
                    maxPolarAngle: (Math.PI / 2) * (props.mapConfig?.otherConfig?.maxAngle || 60) * 0.01, // 限制相机最大俯仰角，防止翻转
                }
            },
            (url, loaded, total) => {
                loadProgress.value = loaded;
                emit('load-progress', loadProgress.value);
            },
            () => {
                // 所有纹理加载完毕
                setTimeout(() => {
                    isLoading.value = false;
                    emit('load-finish');
                }, 500); // 延迟一点消失，体验更好
            }
        );
        baseEarthRef.value = earthInstance;

        // 渲染地图数据
        const data = props.mapData || provinceJson;
        await earthInstance.updateMap(data);

        // 启动渲染循环
        earthInstance.run();

        window.addEventListener('resize', handleResize);

        // 使用 ResizeObserver 监听容器大小变化
        if (mapContainerRef.value) {
            resizeObserver = new ResizeObserver(() => {
                // 使用 requestAnimationFrame 确保在下一帧执行，避免频繁触发
                requestAnimationFrame(() => {
                    handleResize();
                });
            });
            resizeObserver.observe(mapContainerRef.value);
        }

    } catch (error) {
        console.error('3d地图加载错误:', error);
        errorMessage.value = error instanceof Error ? error.message : '未知错误';
        isLoading.value = false;
        emit('render-error', errorMessage.value);
    }
});

// 监听数据变化
watch(() => props, async (val) => {
    if (baseEarthRef.value && val) {
        emit('load-start');
        isLoading.value = true;
        try {
            await baseEarthRef.value.updateMap(val.mapData);
            // 数据更新不需要重新加载纹理，所以手动关闭 loading
            isLoading.value = false;
            emit('load-finish');
        } catch (e) {
            emit('render-error', '数据更新失败');
            isLoading.value = false;
        }
    }
}, { deep: true });

/**
 * 组件卸载前清理资源
 * 移除事件监听器，防止内存泄漏
 */
onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize);
    // 清理 ResizeObserver
    if (resizeObserver && mapContainerRef.value) {
        resizeObserver.unobserve(mapContainerRef.value);
        resizeObserver.disconnect();
        resizeObserver = null;
    }
    if (baseEarthRef.value) {
        baseEarthRef.value.destroy();
        baseEarthRef.value = null; // 解除引用
    }
});
</script>
<style>
/* 全屏样式：确保地图容器占满整个视口 */
.map-container {
    width: 100%;
    height: 100%;
    position: relative;
    background: #000;
    /* 兜底背景色 */
}

.is-full {
    width: 100%;
    height: 100%;
    overflow: hidden;
    /* 隐藏滚动条 */
}

/* 地图标签样式 */
.map-32-label {
    font-size: 10px;
    color: #fff;
    /* 白色文字 */
}

/* Loading 遮罩层 */
.loading-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.8);
    backdrop-filter: blur(4px);
    z-index: 999;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #fff;
}

.loading-content {
    text-align: center;
}

.spinner {
    width: 40px;
    height: 40px;
    border: 4px solid rgba(255, 255, 255, 0.3);
    border-top: 4px solid #00ffff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 16px;
}

.loading-text {
    font-size: 14px;
    letter-spacing: 1px;
}

.error-tip {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #ff4d4f;
    background: rgba(0, 0, 0, 0.8);
    padding: 12px 24px;
    border-radius: 4px;
    z-index: 1000;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>
