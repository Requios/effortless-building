package nl.requios.effortlessbuilding.buildmode;

import nl.requios.effortlessbuilding.AllIcons;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Circle;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Cube;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Cylinder;
import nl.requios.effortlessbuilding.buildmode.buildmodes.DiagonalLine;
import nl.requios.effortlessbuilding.buildmode.buildmodes.DiagonalWall;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Disabled;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Floor;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Line;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Single;
import nl.requios.effortlessbuilding.buildmode.buildmodes.SlopeFloor;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Sphere;
import nl.requios.effortlessbuilding.buildmode.buildmodes.Wall;

public enum BuildModeEnum {
    DISABLED("normal", new Disabled(), BuildModeCategoryEnum.BASIC, AllIcons.I_DISABLE),
    SINGLE("normal_plus", new Single(), BuildModeCategoryEnum.BASIC, AllIcons.I_SINGLE, ModeOptions.OptionEnum.BUILD_SPEED),
    LINE("line", new Line(), BuildModeCategoryEnum.BASIC, AllIcons.I_LINE /*, OptionEnum.THICKNESS*/),
    WALL("wall", new Wall(), BuildModeCategoryEnum.BASIC, AllIcons.I_WALL, ModeOptions.OptionEnum.FILL),
    FLOOR("floor", new Floor(), BuildModeCategoryEnum.BASIC, AllIcons.I_FLOOR, ModeOptions.OptionEnum.FILL),
    CUBE("cube", new Cube(), BuildModeCategoryEnum.BASIC, AllIcons.I_CUBE, ModeOptions.OptionEnum.CUBE_FILL),
    DIAGONAL_LINE("diagonal_line", new DiagonalLine(), BuildModeCategoryEnum.DIAGONAL, AllIcons.I_DIAGONAL_LINE /*, OptionEnum.THICKNESS*/),
    DIAGONAL_WALL("diagonal_wall", new DiagonalWall(), BuildModeCategoryEnum.DIAGONAL, AllIcons.I_DIAGONAL_WALL /*, OptionEnum.FILL*/),
    SLOPE_FLOOR("slope_floor", new SlopeFloor(), BuildModeCategoryEnum.DIAGONAL, AllIcons.I_SLOPED_FLOOR, ModeOptions.OptionEnum.RAISED_EDGE),
    CIRCLE("circle", new Circle(), BuildModeCategoryEnum.CIRCULAR, AllIcons.I_CIRCLE, ModeOptions.OptionEnum.CIRCLE_START, ModeOptions.OptionEnum.FILL),
    CYLINDER("cylinder", new Cylinder(), BuildModeCategoryEnum.CIRCULAR, AllIcons.I_CYLINDER, ModeOptions.OptionEnum.CIRCLE_START, ModeOptions.OptionEnum.FILL),
    SPHERE("sphere", new Sphere(), BuildModeCategoryEnum.CIRCULAR, AllIcons.I_SPHERE, ModeOptions.OptionEnum.CIRCLE_START, ModeOptions.OptionEnum.FILL);
//		PYRAMID("pyramid", new Pyramid(), BuildModeCategoryEnum.ROOF),
//		CONE("cone", new Cone(), BuildModeCategoryEnum.ROOF),
//		DOME("dome", new Dome(), BuildModeCategoryEnum.ROOF);

    private final String name;
    public final IBuildMode instance;
    public final BuildModeCategoryEnum category;
    public final AllIcons icon;
    public final ModeOptions.OptionEnum[] options;

    BuildModeEnum(String name, IBuildMode instance, BuildModeCategoryEnum category, AllIcons icon, ModeOptions.OptionEnum... options) {
        this.name = name;
        this.instance = instance;
        this.category = category;
        this.icon = icon;
        this.options = options;
    }

    public String getNameKey() {
        return "effortlessbuilding.mode." + name;
    }

    public String getDescriptionKey() {
        return "effortlessbuilding.modedescription." + name;
    }

}
