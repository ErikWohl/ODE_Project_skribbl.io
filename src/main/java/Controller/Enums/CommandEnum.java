package Controller.Enums;

public enum CommandEnum {
    // Controller Commands
    MESSAGE("MSG"),
    DRAWING("DRW"),
    CLEAR("CLR"),

    // Game Service Commands
    START_GAME_REQUEST("SGR"),
    START_GAME_ACKNOWLEDGEMENT("SGA"),
    START_GAME_NOTACKNOWLEDGEMENT("SGN"),
    GUESSER_REQUEST("GSR"),
    DRAWER_REQUEST("DWR"),
    DRAWER_ACKNOWLEDGEMENT("DWA"),
    ROUND_START_REQUEST("RSR"),
    ROUND_START_ACKNOWLEDGEMENT("RSA"),
    ROUND_START_NOTACKNOWLEDGEMENT("RSN"),
    ROUND_STARTED("RST"),
    CLOSE_GUESS("CLG"),
    CORRECT_GUESS("CRG"),
    ROUND_END_SUCCESS("RES"),
    ROUND_END_TIMEOUT("RET"),
    ROUND_END_ACKNOWLEDGEMENT("REA"),
    ERROR("ERR");

    private final String command;
    private CommandEnum(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
    public static CommandEnum fromString(String text) {
        for(CommandEnum c : CommandEnum.values()) {
            if(c.command.equals(text)) {
                return c;
            }
        }
        throw new IllegalArgumentException();
    }
}
