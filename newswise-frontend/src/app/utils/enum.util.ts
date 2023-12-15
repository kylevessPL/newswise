export default class EnumUtil {
    static getEnumValue = (enumClass: any, key: string): string | undefined => {
        const enumKeys = Object.keys(enumClass);
        return enumKeys.includes(key) ? enumClass[key] : undefined;
    };
}
