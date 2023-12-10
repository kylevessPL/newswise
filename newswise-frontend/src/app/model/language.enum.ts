export class Language {
    static readonly ENGLISH = new Language('en', 'English');

    private constructor(public readonly code: string, public readonly localizedName: string) {
    }

    static getByCode = (code: string): Language | undefined => Object.values(Language)
        .filter(value => value instanceof Language)
        .find(lang => lang.code === code);
}
