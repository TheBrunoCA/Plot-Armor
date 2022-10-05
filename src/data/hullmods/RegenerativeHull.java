package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class RegenerativeHull extends BaseHullMod {

    private final float regenPerItter = 1f;
    private final float itterIntervalInSecs = 1f;

    long timer = System.currentTimeMillis();
    long now = System.currentTimeMillis();
    long elapsed = 0;

    @Override
public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        now = System.currentTimeMillis();

        elapsed = now - timer;

        if(hullDamaged(ship) && elapsed >= itterIntervalInSecs * 1000f){
            timer = System.currentTimeMillis();

            regenHull(ship, regenPerItter);
        }

        if(hullOverflowing(ship)){
            ship.setHitpoints(ship.getMaxHitpoints());
        }
    }

    private static boolean hullOverflowing(ShipAPI ship) {
        return ship.getHitpoints() > ship.getMaxHitpoints();
    }

    private static void regenHull(ShipAPI ship, float regenPerItter) {
        float newHitpoints = ship.getHitpoints() + ( ship.getMaxHitpoints() * ( regenPerItter / 100f ) );
        ship.setHitpoints( newHitpoints );
    }

    private static boolean hullDamaged(ShipAPI ship){
        boolean x = ship.getHitpoints() < ship.getMaxHitpoints();
        return x;
    }
}
