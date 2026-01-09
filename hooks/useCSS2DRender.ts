import { CSS2DObject, CSS2DRenderer } from 'three/examples/jsm/renderers/CSS2DRenderer.js'
import * as THREE from 'three'

export default function useCSS2DRender() {
    /**
     * 【已废弃】初始化CSS2D标签渲染器
     * CSS2D标签会固定在相机上，不会跟随区块旋转
     * @deprecated 请使用 createSpriteTag 创建Sprite标签，可以贴在区块面上
     * @param {*} options 参数
     * @param {*} container canvas内容容器
     * @returns
     */
    const initCSS2DRender = (options: any, container: any) => {
        let { width, height } = options // 获取世界的宽高
        let css2dRender = new CSS2DRenderer() // 实例化css2d渲染器
        css2dRender.setSize(width, height) // 设置渲染器的尺寸
        css2dRender.domElement.style.position = 'absolute' // 设置定位位置
        css2dRender.domElement.style.left = '0px'
        css2dRender.domElement.style.top = '0px'
        css2dRender.domElement.style.pointerEvents = 'none' // 设置不能背选中
        container.appendChild(css2dRender.domElement) // 插入到容器当中
        return css2dRender
    }
    /**
     * 【已废弃】创建CSS2D标签
     * CSS2D标签会固定在相机上，不会跟随区块旋转
     * @deprecated 请使用 createSpriteTag 创建Sprite标签，可以贴在区块面上
     * @param {*} name  标签内容
     * @param {*} className 标签class
     * @returns
     */
    const create2DTag = (name = '', className = '') => {
        let tag = document.createElement('div')
        tag.innerHTML = name
        tag.className = className
        tag.style.pointerEvents = 'none'
        tag.style.visibility = 'hidden'
        tag.style.position = 'absolute'
        // 如果className不存在，用以下样式
        if (!className) {
            tag.style.padding = '10px'
            tag.style.color = '#fff'
            tag.style.fontSize = '12px'
            tag.style.textAlign = 'center'
            tag.style.background = 'rgba(0,0,0,0.6)'
            tag.style.borderRadius = '4px'
        }
        let label = new CSS2DObject(tag) as any
        /**
         * 标签显示，
         * @param {*} name 显示内容
         * @param {*} point 显示坐标
         */
        label.show = (name: string, point: any) => {
            label.element.innerHTML = name
            label.element.style.visibility = 'visible'
            label.position.copy(point)
        }
        /**
         * 隐藏
         */
        label.hide = () => {
            label.element.style.visibility = 'hidden'
        }
        return label
    }

    /**
     * 创建文本纹理（使用Canvas）
     * 将文本绘制到Canvas上，然后转换为Three.js纹理
     * @param text - 要显示的文本
     * @param fontSize - 字体大小，默认20
     * @param fontColor - 字体颜色，默认白色
     * @param backgroundColor - 背景颜色，默认半透明黑色
     * @returns Three.js纹理对象
     */
    const createTextTexture = (
        text: string,
        fontSize: number = 20,
        fontColor: string = '#ffffff',
        backgroundColor: string = 'rgba(0, 0, 0, 0.6)'
    ): THREE.Texture => {
        const canvas = document.createElement('canvas')
        const context = canvas.getContext('2d')
        if (!context) {
            throw new Error('无法创建Canvas上下文')
        }

        // 设置字体
        context.font = `bold ${fontSize}px Arial`
        context.textAlign = 'center'
        context.textBaseline = 'middle'

        // 测量文本尺寸
        const metrics = context.measureText(text)
        const textWidth = metrics.width
        const textHeight = fontSize

        // 设置Canvas尺寸（添加内边距）
        const padding = 10
        canvas.width = textWidth + padding * 2
        canvas.height = textHeight + padding * 2

        // 重新设置字体（因为Canvas尺寸改变了）
        context.font = `bold ${fontSize}px Arial`
        context.textAlign = 'center'
        context.textBaseline = 'middle'

        // 绘制背景
        if (backgroundColor) {
            context.fillStyle = backgroundColor
            context.fillRect(0, 0, canvas.width, canvas.height)
        }

        // 绘制文本
        context.fillStyle = fontColor
        context.fillText(text, canvas.width / 2, canvas.height / 2)

        // 创建纹理
        const texture = new THREE.CanvasTexture(canvas)
        texture.needsUpdate = true
        return texture
    }

    /**
     * 创建Sprite标签（贴在区块面上）
     * 使用Sprite材质创建标签，标签会始终面向相机，但位置会跟随区块的变换
     * 将标签添加到区块对象上作为子对象，这样标签会跟随区块旋转
     * @param text - 标签文本内容
     * @param position - 标签位置 [x, y, z] 或 THREE.Vector3
     * @param options - 可选配置项
     * @param options.fontSize - 字体大小，默认20
     * @param options.fontColor - 字体颜色，默认白色
     * @param options.backgroundColor - 背景颜色，默认半透明黑色
     * @param options.scale - 标签缩放比例，默认0.5
     * @returns THREE.Sprite对象
     */
    const createSpriteTag = (
        text: string,
        position: [number, number, number] | THREE.Vector3,
        options?: {
            fontSize?: number
            fontColor?: string
            backgroundColor?: string
            scale?: number
        }
    ): THREE.Sprite => {
        const {
            fontSize = 20,
            fontColor = '#ffffff',
            backgroundColor = 'rgba(0, 0, 0, 0.6)',
            scale = 0.5,
        } = options || {}

        // 创建文本纹理
        const texture = createTextTexture(text, fontSize, fontColor, backgroundColor)

        // 创建Sprite材质
        const spriteMaterial = new THREE.SpriteMaterial({
            map: texture,
            transparent: true,
            alphaTest: 0.1,
        })

        // 创建Sprite对象
        const sprite = new THREE.Sprite(spriteMaterial)

        // 设置标签位置
        if (position instanceof THREE.Vector3) {
            sprite.position.copy(position)
        } else {
            sprite.position.set(position[0], position[1], position[2])
        }

        // 设置标签大小
        sprite.scale.set(scale, scale * 0.5, 1)

        return sprite
    }

    return {
        // 废弃的方法（保留以兼容旧代码）
        initCSS2DRender,
        create2DTag,
        // 新的方法
        createTextTexture,
        createSpriteTag,
    }
}
