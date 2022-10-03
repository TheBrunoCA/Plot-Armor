package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class SmartAlloy extends BaseHullMod {

    private float redistributionPerItter = 10f;
    private float itterIntervalInSecs = 1f;

    long timer = System.currentTimeMillis();
    long now = System.currentTimeMillis();
    long elapsed = 0;

    @Override
public void advanceInCombat(ShipAPI ship, float amount) {
        if(!ship.isAlive()) return;
        super.advanceInCombat(ship, amount);

        now = System.currentTimeMillis();

        elapsed = now - timer;

        if(elapsed >= itterIntervalInSecs * 1000f){
            timer = System.currentTimeMillis();

            float shipArmor[][] = getArmorGrid(ship);
            int minFoundArmor[] = {0,0};
            int maxFoundArmor[] = {1,1};

            for(int x = 0; x < shipArmor.length; x++){
                for(int y = 0; y < shipArmor[x].length; y++){

                    float armor = getArmorValue(ship, x, y);
                    float maxArmor = getMaxArmorInCell(ship);
                    
                    if(armor < getArmorValue(ship, minFoundArmor[0], minFoundArmor[1])){
                        minFoundArmor[0] = x;
                        minFoundArmor[1] = y;
                    } else if (armor > getArmorValue(ship, maxFoundArmor[0], maxFoundArmor[1])) {
                        maxFoundArmor[0] = x;
                        maxFoundArmor[1] = y;
                    }
                }
            }
            if(getArmorValue(ship, minFoundArmor[0], minFoundArmor[1]) >
                    getArmorValue(ship, maxFoundArmor[0], minFoundArmor[1]) * (1 - (redistributionPerItter/100f))){
                return;
            }
            float max = getArmorValue(ship, maxFoundArmor[0], maxFoundArmor[1]);
            float min = getArmorValue(ship, minFoundArmor[0], minFoundArmor[1]);

            setArmorValue(ship, minFoundArmor, min);
            setArmorValue(ship, maxFoundArmor, max);

        }

        if(hullOverflowing(ship)){
            ship.setHitpoints(ship.getMaxHitpoints());
        }
    }

    private void setArmorValue(ShipAPI ship, int[] foundArmor, float value) {
        ship.getArmorGrid().setArmorValue(foundArmor[0], foundArmor[1], value + (getMaxArmorInCell(ship) * (redistributionPerItter/100f)));
    }

    private static float[][] getArmorGrid(ShipAPI ship) {
        return ship.getArmorGrid().getGrid();
    }

    private static float getMaxArmorInCell(ShipAPI ship) {
        return ship.getArmorGrid().getMaxArmorInCell();
    }

    private static float getArmorValue(ShipAPI ship, int x, int y) {
        return ship.getArmorGrid().getArmorValue(x, y);
    }

    private boolean hullOverflowing(ShipAPI ship) {
        return ship.getHitpoints() > ship.getMaxHitpoints();
    }

    private static void regenHull(ShipAPI ship, float regenPerItter) {
        float newHitpoints = ship.getHitpoints() + ( ship.getMaxHitpoints() * ( regenPerItter / 100f ) );
        ship.setHitpoints( newHitpoints );
    }

    private boolean hullDamaged(ShipAPI ship){
        boolean x = ship.getHitpoints() < ship.getMaxHitpoints();
        return x;
    }
}
