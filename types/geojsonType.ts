interface GeoFeatureProperties {
    adcode: number;
    name: string;
    center: number[];
    centroid: number[];
    childrenNum: number;
    level: string;
    parent: {
        adcode: number;
    };
    acroutes: number[];
}

interface GeoFeature {
    type: string;
    properties: GeoFeatureProperties;
    geometry: {
        type: string;
        coordinates: number[][][][]; // 多层嵌套坐标
    };
}

export interface GeoJSONData {
    type: string;
    features: GeoFeature[];
}