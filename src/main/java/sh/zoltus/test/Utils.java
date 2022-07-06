package sh.zoltus.test;

public class Utils {

    public static Skill getCombatLevelPrecise(double attackLevel, double strengthLevel,
                                              double defenceLevel, double hitpointsLevel, double magicLevel,
                                              double rangeLevel, double prayerLevel) {
        double base = 0.25 * (defenceLevel + hitpointsLevel + (prayerLevel / 2));

        double typeContribution = getMeleeRangeOrMagicCombatLevelContribution(attackLevel, strengthLevel, magicLevel, rangeLevel);

        return new Skill(0, (int) (base + typeContribution), 0);
    }

    private static double getMeleeRangeOrMagicCombatLevelContribution(double attackLevel, double strengthLevel, double magicLevel, double rangeLevel) {
        double melee = 0.325 * (attackLevel + strengthLevel);
        double range = 0.325 * (rangeLevel / 2 + rangeLevel);
        double magic = 0.325 * (magicLevel / 2 + magicLevel);

        return Math.max(melee, Math.max(range, magic));
    }
}
