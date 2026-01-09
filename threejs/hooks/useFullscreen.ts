import { ref, onMounted, onUnmounted, type Ref } from 'vue';

export default function useFullscreen(targetRef: Ref<HTMLElement | null>) {
    const isFullscreen = ref(false);

    /**
     * 进入全屏
     */
    const enter = async () => {
        const el = targetRef.value;
        if (!el) return;

        try {
            if (el.requestFullscreen) {
                await el.requestFullscreen();
            } else if ((el as any).mozRequestFullScreen) { // Firefox
                await (el as any).mozRequestFullScreen();
            } else if ((el as any).webkitRequestFullscreen) { // Chrome, Safari and Opera
                await (el as any).webkitRequestFullscreen();
            } else if ((el as any).msRequestFullscreen) { // IE/Edge
                await (el as any).msRequestFullscreen();
            }
            isFullscreen.value = true;
        } catch (error) {
            console.error('进入全屏失败:', error);
        }
    };

    /**
     * 退出全屏
     */
    const exit = async () => {
        try {
            if (document.exitFullscreen) {
                await document.exitFullscreen();
            } else if ((document as any).mozCancelFullScreen) {
                await (document as any).mozCancelFullScreen();
            } else if ((document as any).webkitExitFullscreen) {
                await (document as any).webkitExitFullscreen();
            } else if ((document as any).msExitFullscreen) {
                await (document as any).msExitFullscreen();
            }
            isFullscreen.value = false;
        } catch (error) {
            console.error('退出全屏失败:', error);
        }
    };

    /**
     * 切换全屏状态
     */
    const toggle = () => {
        if (isFullscreen.value) {
            exit();
        } else {
            enter();
        }
    };

    /**
     * 监听全屏变化事件（处理用户按 ESC 键退出的情况）
     */
    const handleFullscreenChange = () => {
        const fullscreenElement = document.fullscreenElement ||
            (document as any).webkitFullscreenElement ||
            (document as any).mozFullScreenElement ||
            (document as any).msFullscreenElement;

        // 如果全屏元素是当前目标，或者是当前目标的子元素，或者是 null (退出全屏)
        isFullscreen.value = !!fullscreenElement && fullscreenElement === targetRef.value;
    };

    onMounted(() => {
        document.addEventListener('fullscreenchange', handleFullscreenChange);
        document.addEventListener('webkitfullscreenchange', handleFullscreenChange);
        document.addEventListener('mozfullscreenchange', handleFullscreenChange);
        document.addEventListener('MSFullscreenChange', handleFullscreenChange);
    });

    onUnmounted(() => {
        document.removeEventListener('fullscreenchange', handleFullscreenChange);
        document.removeEventListener('webkitfullscreenchange', handleFullscreenChange);
        document.removeEventListener('mozfullscreenchange', handleFullscreenChange);
        document.removeEventListener('MSFullscreenChange', handleFullscreenChange);
    });

    return {
        isFullscreen,
        enter,
        exit,
        toggle
    };
}