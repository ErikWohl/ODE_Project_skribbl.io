package Controller.Enums;

public enum LanguageEnum {
    // Controller Commands
    GERMAN("de"),
    ENGLISH("en"),
    SPANISH("es");
    private final String locale;
    private LanguageEnum(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }
    public static LanguageEnum fromString(String text) {
        for(LanguageEnum c : LanguageEnum.values()) {
            if(c.locale.equals(text)) {
                return c;
            }
        }
        throw new IllegalArgumentException();
    }
}
