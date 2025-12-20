package net.alterorb.launcher.model;

public enum FunorbGame {

    GEOBLOX("", "geoblox"),
    LEXICOMINOS("", "lexicominos"),
    BOUNCEDOWN("", "bouncedown"),
    DR_P_SAVES_THE_EARTH("", "drphlogistonsavestheearth"),
    MINER_DISTURBANCE("", "minerdisturbance"),
    WIZARD_RUN("", "wizardrun"),
    THIRTY_SIX_CARD_TRICK("", "36cardtrick"),
    HOLD_THE_LINE("", "holdtheline"),
    STARCANNON("", "starcannon"),
    STELLAR_SHARD("", "stellarshard"),
    TORQUING("", "torquing"),
    THE_TRACK_CONTROLLER("", "trackcontroller"),
    TRANSMOGRIFY("", "transmogrify"),
    CHESS("", "chess"),
    CRAZY_CRYSTALS("", "crazycrystals"),
    SOL_KNIGHT("", "solknight"),
    TETRALINK("", "tetralink"),
    ACE_OF_SKIES("", "aceofskies"),
    ARCANISTS("", "arcanistsmulti"),
    CONFINED("", "confined"),
    ESCAPE_VECTOR("", "escapevector"),
    FLEA_CIRCUS("", "fleacircus"),
    HOSTILE_SPAWN("", "hostilespawn"),
    MONKEY_PUZZLE_2("", "monkeypuzzle2"),
    SHATTERED_PLANS("", "shatteredplans"),
    TERRAPHOENIX("", "terraphoenix"),
    DEKO_BLOKO("", "dekobloko"),
    DUNGEON_ASSAULT("", "dungeonassault"),
    TOR_CHALLENGE("", "torchallenge"),
    ZOMBIE_DAWN("", "zombiedawn"),
    PIXELATE("", "pixelate"),
    ORB_DEFENCE("", "orbdefence"),
    POOL("", "pool"),
    VERTIGO_2("", "vertigo2"),
    BRICK_A_BRAC("", "brickabrac"),
    ARMIES_OF_GIELINOR("", "armiesofgielinor"),
    KICKABOUT_LEAGUE("", "kickabout"),
    BACHELOR_FRIDGE("", "bachelorfridge"),
    STEEL_SENTINELS("", "steelsentinels"),
    ZOMBIE_DAWN_MULTI("", "zombiedawnmulti"),
    VIROGRID("", "virogrid"),
    TOMB_RACER("", "tombracer"),
    VOID_HUNTERS("", "voidhunters"),
    SUMOBLITZ("", "sumoblitz");

    private final String name;
    private final String internalName;

    FunorbGame(String name, String internalName) {
        this.name = name;
        this.internalName = internalName;
    }

    public String fancyName() {
        return name;
    }

    public String internalName() {
        return internalName;
    }
}