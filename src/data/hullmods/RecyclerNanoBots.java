package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class RecyclerNanoBots extends BaseHullMod {

    private float regenPerItter = 1f;
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

        if(hullDamaged(ship) && elapsed >= itterIntervalInSecs * 1000f){
            timer = System.currentTimeMillis();

            float[][] shipArmor = ship.getArmorGrid().getGrid();
            float regen = regenPerItter / (shipArmor.length * shipArmor[0].length);

            for(int x = 0; x < shipArmor.length; x++){
                for(int y = 0; y < shipArmor[x].length; y++){

                    float armor = ship.getArmorGrid().getArmorValue(x, y);
                    float maxArmor = ship.getArmorGrid().getMaxArmorInCell();
                    if(armor < maxArmor * 0.01f) continue;

                    regenHull(ship, regen);
                    ship.getArmorGrid().setArmorValue(x, y, armor - (maxArmor * 0.005f) );

                }
            }
        }

        if(hullOverflowing(ship)){
            ship.setHitpoints(ship.getMaxHitpoints());
        }
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
