import * as d3 from 'd3-geo';

export interface TransformOptions {
    /** 转换后的地图大致宽度（默认100），用于归一化尺寸 */
    mapSize?: number;
}

export default function useGeoJSONTransform() {

    /**
     * 将 GeoJSON 的经纬度转为平面坐标 (墨卡托投影)
     * 同时修复数据结构和中心点
     */
    const transformGeoJSON = (data: any, options: TransformOptions = {}) => {
        // 深拷贝数据，避免污染源数据
        const geoJSON = JSON.parse(JSON.stringify(data));
        const { mapSize = 100 } = options;

        // 1. 创建投影器
        // fitSize 会自动计算缩放和平移，使地图完美填充在 mapSize x mapSize 的区域内
        // 并自动居中到 (0,0) 附近
        const projection = d3.geoMercator().fitSize([mapSize, mapSize], geoJSON);

        // 获取路径生成器（用于计算新的中心点）
        const pathGenerator = d3.geoPath().projection(projection);

        geoJSON.features.forEach((feature: any) => {
            // A. 修复数据结构：强制转为 MultiPolygon (兼容天地图)
            if (feature.geometry.type === 'Polygon') {
                feature.geometry.type = 'MultiPolygon';
                feature.geometry.coordinates = [feature.geometry.coordinates];
            }

            // B. 转换几何坐标 (Geometry Coordinates)
            // 递归遍历坐标数组进行转换
            const transformCoords = (coords: any[]) => {
                // 判断是否是坐标点 [lng, lat]
                if (coords.length === 2 && typeof coords[0] === 'number') {
                    const [lng, lat] = coords;
                    const projected = projection([lng, lat]);
                    if (projected) {
                        // d3 输出的是屏幕坐标(y向下)，Three.js 平面是 x,y (z向上)
                        // 这里我们直接映射到 x,y，Three.js 中会躺平
                        coords[0] = projected[0] - mapSize / 2; // 居中修正 X
                        coords[1] = (mapSize / 2) - projected[1]; // 居中修正 Y，并反转 Y 轴 (因为屏幕坐标Y向下，3D世界Y向上)
                    }
                } else {
                    // 继续递归
                    coords.forEach((item) => transformCoords(item));
                }
            };
            transformCoords(feature.geometry.coordinates);

            // C. 核心修复：处理属性中的中心点 (Properties Centroid)
            // JSON 里的 centroid，就只做投影转换
            const [lng, lat] = feature.properties.centroid;
            const projected = projection([lng, lat]);
            if (projected) {
                feature.properties.centroid = [
                    projected[0] - mapSize / 2,
                    (mapSize / 2) - projected[1]
                ];
            }
        });

        return geoJSON;
    };

    return {
        transformGeoJSON
    };
}