import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import TWEEN from '@tweenjs/tween.js';
import { deepMerge, isType } from './common';

interface Earth3dOptions {
    isFull?: boolean;
    container?: string | null;
    width?: number;
    height?: number;
    bgColor?: number;
    materialColor?: number;
    controls?: {
        visibel?: boolean;
        enableDamping?: boolean;
        autoRotate?: boolean;
        maxPolarAngle?: number;
    };
    statsVisibel?: boolean;
    axesVisibel?: boolean;
    axesHelperSize?: number;
}

export default class Earth3d {
    options: Required<Earth3dOptions>;
    container: HTMLElement;
    scene: THREE.Scene;
    camera: THREE.PerspectiveCamera | null;
    renderer: THREE.WebGLRenderer | null;
    mesh: THREE.Mesh | null;
    animationStop: number | null;
    controls: OrbitControls | null;

    constructor(options: Earth3dOptions = {}) {
        let defaultOptions: Required<Earth3dOptions> = {
            isFull: true,
            container: null,
            width: window.innerWidth,
            height: window.innerHeight,
            bgColor: 0x000000,
            materialColor: 0xff0000,
            controls: {
                visibel: true, // 是否开启
                enableDamping: true, // 阻尼
                autoRotate: false, // 自动旋转
                maxPolarAngle: Math.PI, // 相机垂直旋转角度的上限
            },
            statsVisibel: true,
            axesVisibel: true,
            axesHelperSize: 250, // 左边尺寸
        };
        this.options = deepMerge(defaultOptions, options) as Required<Earth3dOptions>;
        const containerElement = document.querySelector(this.options.container as string) as HTMLElement;
        if (!containerElement) {
            throw new Error('containerElement 未找到,请检查container是否正确');
        }
        this.container = containerElement;
        this.options.width = this.container.offsetWidth;
        this.options.height = this.container.offsetHeight;
        this.scene = new THREE.Scene(); // 场景
        this.camera = null; // 相机
        this.renderer = null; // 渲染器
        this.mesh = null; // 网格
        this.animationStop = null; // 用于停止动画
        this.controls = null; // 轨道控制器

        this.init();
    }
    init() {
        this.initCamera();
        // this.initModel(); // 注释掉自动调用，由外部手动调用以支持 async/await
        this.initRenderer();
        this.initAxes();
        // this.initControls();
    }
    async initModel() { }

    /**
     * 运行
     */
    run() {
        this.loop();
    }
    // 循环
    loop() {
        this.animationStop = window.requestAnimationFrame(() => {
            this.loop();
        });
        if (this.renderer && this.camera) {
            this.renderer.render(this.scene, this.camera);
        }
        // 控制相机旋转缩放的更新
        if (this.options.controls.visibel && this.controls) {
            this.controls.update();
        }

        TWEEN.update();
    }
    initCamera() {
        const { width, height } = this.options;
        const rate = width / height;
        // 设置45°的透视相机,更符合人眼观察
        this.camera = new THREE.PerspectiveCamera(45, rate, 0.1, 1500);
        // this.camera.position.set(-428.88, 861.97, -1438.0)
        this.camera.position.set(270.27, 173.24, 257.54);
        // this.camera.position.set(-102, 205, -342)

        this.camera.lookAt(0, 0, 0);
    }
    /**
     * 初始化渲染器
     */
    initRenderer() {
        let { width, height, bgColor } = this.options;
        let renderer = new THREE.WebGLRenderer({
            antialias: true, // 锯齿
        });
        // 设置canvas的分辨率
        renderer.setPixelRatio(window.devicePixelRatio);
        // 设置canvas 的尺寸大小
        renderer.setSize(width, height);
        // 设置背景色
        renderer.setClearColor(bgColor, 1);
        // 插入到dom中
        this.container.appendChild(renderer.domElement);
        this.renderer = renderer;
    }
    initControls() {
        try {
            let {
                controls: { enableDamping, autoRotate, visibel, maxPolarAngle },
            } = this.options;

            // 如果已存在控制器，先销毁
            if (this.controls) {
                this.controls.dispose();
                this.controls = null;
            }

            if (!visibel || !this.camera || !this.renderer) return false;
            // 轨道控制器，使相机围绕目标进行轨道运动（旋转|缩放|平移）
            this.controls = new OrbitControls(this.camera, this.renderer.domElement);
            this.controls.maxPolarAngle = maxPolarAngle ?? Math.PI;
            this.controls.autoRotate = autoRotate ?? false;
            this.controls.enableDamping = enableDamping ?? true;
        } catch (error) {
            console.log(error);
        }
    }
    initAxes() {
        if (!this.options.axesVisibel) return false;
        var axes = new THREE.AxesHelper(this.options.axesHelperSize);
        this.addObject(axes);
    }
    /**
     * 添加对象到场景
     * @param {*} object  {} []
     */
    addObject(object: THREE.Object3D | THREE.Object3D[]) {
        if (isType('Array', object)) {
            this.scene.add(...(object as THREE.Object3D[]));
        } else {
            this.scene.add(object as THREE.Object3D);
        }
    }
    /**
     * 移除对象
     * @param {*} object {} []
     */
    removeObject(object: THREE.Object3D | THREE.Object3D[]) {
        if (isType('Array', object)) {
            (object as THREE.Object3D[]).forEach((item: THREE.Object3D) => {
                if ('geometry' in item && item.geometry) {
                    (item.geometry as THREE.BufferGeometry).dispose();
                }
            });
            this.scene.remove(...(object as THREE.Object3D[]));
        } else {
            if ('geometry' in object && object.geometry) {
                (object.geometry as THREE.BufferGeometry).dispose();
            }
            this.scene.remove(object as THREE.Object3D);
        }
    }
    /**
     * 重置
     */
    resize() {
        // 重新设置宽高
        this.options.width = this.container.offsetWidth || window.innerWidth;
        this.options.height = this.container.offsetHeight || window.innerHeight;

        if (this.renderer) {
            this.renderer.setSize(this.options.width, this.options.height);
        }
        // 重新设置相机的位置
        if (this.camera) {
            const rate = this.options.width / this.options.height;

            // 必須設置相機的比例，重置的時候才不会变形
            this.camera.aspect = rate;

            // 渲染器执行render方法的时候会读取相机对象的投影矩阵属性projectionMatrix
            // 但是不会每渲染一帧，就通过相机的属性计算投影矩阵(节约计算资源)
            // 如果相机的一些属性发生了变化，需要执行updateProjectionMatrix ()方法更新相机的投影矩阵
            this.camera.updateProjectionMatrix();
        }
    }

    /**
     * 销毁
     */
    destroy() {
        // 1. 停止动画循环
        if (this.animationStop) {
            window.cancelAnimationFrame(this.animationStop);
            this.animationStop = null;
        }

        // 2. 销毁控制器
        if (this.controls) {
            this.controls.dispose();
            this.controls = null;
        }

        // 3. 销毁渲染器 DOM
        if (this.renderer) {
            this.renderer.dispose(); // 释放 WebGL 上下文
            if (this.container && this.renderer.domElement) {
                // 这种判断比 empty 方法更安全
                if (this.container.contains(this.renderer.domElement)) {
                    this.container.removeChild(this.renderer.domElement);
                }
            }
            this.renderer = null;
        }

        // 4. 清理场景
        if (this.scene) {
            this.scene.clear();
        }
    }
}
