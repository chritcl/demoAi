import * as THREE from 'three';
/**
 * 经纬度坐标转球面坐标
 * @param {地球半径} R
 * @param {经度(角度值)} longitude
 * @param {维度(角度值)} latitude
 */
export const lon2xyz = (R: number, longitude: number, latitude: number) => {
    var lon = (longitude * Math.PI) / 180; //转弧度值
    var lat = (latitude * Math.PI) / 180; //转弧度值
    lon = -lon; // three.js坐标系z坐标轴对应经度-90度，而不是90度

    // 经纬度坐标转球面坐标计算公式
    var x = R * Math.cos(lat) * Math.cos(lon);
    var y = R * Math.sin(lat);
    var z = R * Math.cos(lat) * Math.sin(lon);
    // 返回球面坐标
    return {
        x: x,
        y: y,
        z: z,
    };
}

export const isType = (type: string, value: any) => {
    return Object.prototype.toString.call(value) === `[object ${type}]`;
};
/**
 * 判断是否为对象
 */
export const isObject = (value: any) => {
    return isType('Object', value);
};
/**
 * @description deepClone() 深拷贝-最终版：解决循环引用的问题
 * @param {*} target 对象
 * @example
 *      const obj1 = {
 *          a: 1,
 *          b: ["e", "f", "g"],
 *          c: { h: { i: 2 } },
 *      };
 *      obj1.b.push(obj1.c);
 *      obj1.c.j = obj1.b;
 *
 *      const obj2 = deepClone(obj1);
 *      obj2.b.push("h");
 *      console.log(obj1, obj2);
 *      console.log(obj2.c === obj1.c);
 */
export const deepClone = (target: any, map = new Map()) => {
    // target 不能为空，并且是一个对象
    if (target != null && isObject(target)) {
        // 在克隆数据前，进行判断是否克隆过,已克隆就返回克隆的值
        let cache = map.get(target);
        if (cache) {
            return cache;
        }
        // 判断是否为数组
        const isArray = Array.isArray(target);
        let result: any = isArray ? [] : {};
        // 将新结果存入缓存中
        cache = map.set(target, result);
        // 如果是数组
        if (isArray) {
            // 循环数组，
            target.forEach((item, index) => {
                // 如果item是对象，再次递归
                result[index] = deepClone(item, cache);
            });
        } else {
            // 如果是对象
            Object.keys(target).forEach((key) => {
                if (isObject(result[key])) {
                    result[key] = deepClone(target[key], cache);
                } else {
                    result[key] = target[key];
                }
            });
        }
        return result;
    } else {
        return target;
    }
}

export const deepMerge = (target: any, source: any) => {
    target = deepClone(target);
    for (let key in source) {
        if (key in target) {
            // 对象的处理
            if (isObject(source[key])) {
                if (!isObject(target[key])) {
                    target[key] = source[key];
                } else {
                    target[key] = deepMerge(target[key], source[key]);
                }
            } else {
                target[key] = source[key];
            }
        } else {
            target[key] = source[key];
        }
    }
    return target;
}
// 随机数
export const random = (min: number, max: number) => {
    return Math.floor(Math.random() * (max - min + 1)) + min;
};
/**
 * 获取样式
 * @param {*} el
 * @param {*} ruleName
 * @returns
 */
export const getStyle = (el: HTMLElement, ruleName: keyof CSSStyleDeclaration) => {
    return window.getComputedStyle(el)[ruleName];
}

/**
 * 从 GeoJSON 中提取所有坐标点
 * @param {*} geojson GeoJSON 对象
 * @returns 坐标点数组 [[lng, lat], ...]
 */
const extractCoordinates = (geojson: any): number[][] => {
    const coordinates: number[][] = [];

    if (!geojson) {
        return coordinates;
    }

    // 处理 FeatureCollection
    if (geojson.type === 'FeatureCollection' && Array.isArray(geojson.features)) {
        geojson.features.forEach((feature: any) => {
            if (feature.geometry) {
                coordinates.push(...extractCoordinates(feature.geometry));
            }
        });
        return coordinates;
    }

    // 处理 Feature
    if (geojson.type === 'Feature' && geojson.geometry) {
        return extractCoordinates(geojson.geometry);
    }

    // 处理 Geometry
    const geometry = geojson;
    if (!geometry.type || !geometry.coordinates) {
        return coordinates;
    }

    const coords = geometry.coordinates;

    switch (geometry.type) {
        case 'Point':
            if (Array.isArray(coords) && coords.length >= 2) {
                coordinates.push([coords[0], coords[1]]);
            }
            break;
        case 'LineString':
        case 'MultiPoint':
            if (Array.isArray(coords)) {
                coords.forEach((coord: any) => {
                    if (Array.isArray(coord) && coord.length >= 2) {
                        coordinates.push([coord[0], coord[1]]);
                    }
                });
            }
            break;
        case 'Polygon':
        case 'MultiLineString':
            if (Array.isArray(coords)) {
                coords.forEach((ring: any) => {
                    if (Array.isArray(ring)) {
                        ring.forEach((coord: any) => {
                            if (Array.isArray(coord) && coord.length >= 2) {
                                coordinates.push([coord[0], coord[1]]);
                            }
                        });
                    }
                });
            }
            break;
        case 'MultiPolygon':
            if (Array.isArray(coords)) {
                coords.forEach((polygon: any) => {
                    if (Array.isArray(polygon)) {
                        polygon.forEach((ring: any) => {
                            if (Array.isArray(ring)) {
                                ring.forEach((coord: any) => {
                                    if (Array.isArray(coord) && coord.length >= 2) {
                                        coordinates.push([coord[0], coord[1]]);
                                    }
                                });
                            }
                        });
                    }
                });
            }
            break;
        case 'GeometryCollection':
            if (Array.isArray(geometry.geometries)) {
                geometry.geometries.forEach((geom: any) => {
                    coordinates.push(...extractCoordinates(geom));
                });
            }
            break;
    }

    return coordinates;
};

/**
 * 计算 GeoJSON 的中心点
 * @param {*} geojson GeoJSON 对象
 * @returns 中心点坐标 { longitude: number, latitude: number }
 */
export const getGeoJsonCenter = (geojson: any): { longitude: number; latitude: number } | null => {
    const coordinates = extractCoordinates(geojson);

    if (coordinates.length === 0) {
        return null;
    }

    // 计算所有坐标点的平均值
    let sumLng = 0;
    let sumLat = 0;

    coordinates.forEach((coord) => {
        sumLng += coord[0];
        sumLat += coord[1];
    });

    return {
        longitude: sumLng / coordinates.length,
        latitude: sumLat / coordinates.length,
    };
}

/**
 * 初始化摄像机位置，使其看向地图中心，并保持初始俯仰角
 * @param camera THREE.Camera - 你的摄像机
 * @param center { x: number, y: number, z?: number } - 地图中心
 * @param distance number - 水平距离（越大视角越远）
 * @param pitch number - 初始俯仰角，单位度（默认 45°）
 */
export const initCameraWithPitch = (camera: THREE.Camera, center: { x: number, y: number, z?: number }, distance: number = 5, pitch: number = 45) => {
    const cx = center.x;
    const cy = center.y;
    const cz = center.z ?? 0;

    // 将角度转为弧度
    const pitchRad = pitch * (Math.PI / 180);

    // z 高度 = d * tan(俯仰角)
    const z = distance * Math.tan(pitchRad);

    // 水平偏移 = distance / sqrt(2)，使摄像机从正对角线看过去
    const offset = distance / Math.sqrt(2);

    camera.position.set(cx + offset, cy + offset, cz + z);
    camera.lookAt(cx, cy, cz);
}