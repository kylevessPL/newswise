export default class UnitUtil {
    static kilobytesPrettyPrint = (size?: number) => {
        const sizes = ['KB', 'MB', 'GB', 'TB'];
        if (!size || size === 0) {
            return 'n/a';
        }
        const i = Math.min(Math.floor(Math.log(size) / Math.log(1024)), sizes.length - 1);
        if (i === 0) {
            return `${size} ${sizes[i]}`;
        }
        const sizeString = (size / (1024 ** i))
            .toFixed(2)
            .replace(/(\d+(\.\d+[1-9])?)(\.?0+$)/, '$1');
        return `${sizeString} ${sizes[i]}`;
    };
}
