package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BiomechanicalHull extends BaseHullMod {

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if(ship.getVariant().hasHullMod("tbca_recycle_bots")){
            return "The Bio-Mechanical Hull will eat the Recycle Bots, but in turn it will receive a bonus in the scarring effect efficiency.";
        }
        return null;
    }

    final Map<ShipAPI.HullSize, Float> regenSpeed = new HashMap<>();
    {
        regenSpeed.put(ShipAPI.HullSize.FIGHTER, 2f);
        regenSpeed.put(ShipAPI.HullSize.FRIGATE, 2f);
        regenSpeed.put(ShipAPI.HullSize.DESTROYER, 1.5f);
        regenSpeed.put(ShipAPI.HullSize.CRUISER, 1f);
        regenSpeed.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.6f);
    }
    final Map<Boolean, Float> scarEffect = new HashMap<>();
    {
        scarEffect.put(false, 5f);
        scarEffect.put(true, 7f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(!ship.isAlive()) return;

        CombatEngineAPI cEngine = Global.getCombatEngine();
        boolean hasRecyclerBots = ship.getVariant().hasHullMod("tbca_recycler_bots");

        Map<String, Object> customData = cEngine.getCustomData();
        String capacityKey = "tbca_biomechanical_hull_capacity"+ship.getId();
        String alreadyRegenKey = "tbca_biomechanical_hull_alreadyRegen"+ship.getId();

        if(customData.get(capacityKey) == null){
            customData.put(capacityKey, ship.getHullLevelAtDeployment());
        }
        if(customData.get(alreadyRegenKey) == null){
            if(ship.getHullLevelAtDeployment() < 1f){
                //noinspection PointlessArithmeticExpression
                float val = ((ship.getHullLevelAtDeployment() / 1f -1f) *-1f) / scarEffect.get(hasRecyclerBots);
                customData.put(alreadyRegenKey, val);
            }
            else{
                customData.put(alreadyRegenKey, 0f);
            }
        }

        float regenCapacity = (float)customData.get(capacityKey);
        float alreadyRegen = (float)customData.get(alreadyRegenKey);

        float remainingHP = ship.getHullLevel();

        if(remainingHP < regenCapacity && remainingHP < ship.getHullLevelAtDeployment()){

            float valToRegen = ship.getMaxHitpoints() * ( regenSpeed.get(ship.getHullSize() ) / 100f ) * amount;

            if(valToRegen > ship.getMaxHitpoints() * regenCapacity - ship.getHitpoints())
                valToRegen = ship.getMaxHitpoints() * regenCapacity - ship.getHitpoints();

            alreadyRegen += valToRegen / ship.getMaxHitpoints();

            regenCapacity = 1f - (alreadyRegen / scarEffect.get(hasRecyclerBots));

            float val = ship.getHitpoints() + valToRegen;

            ship.setHitpoints(val);

            customData.put(capacityKey, regenCapacity);
            customData.put(alreadyRegenKey, alreadyRegen);

        }
        if(!Objects.equals(ship.getId(), cEngine.getPlayerShip().getId()))
            return;

        cEngine.maintainStatusForPlayerShip("tbca_biomechanical_hull_alreadyRegen", "graphics/icons/hullsys/biomechanical_hull.png", "Amount Already Regenerated", (float) Math.round(alreadyRegen * 1000) / 10 + "%", false);
        if(remainingHP >= 1f) {
            cEngine.maintainStatusForPlayerShip("tbca_biomechanical_hull_capacity", "graphics/icons/hullsys/biomechanical_hull.png", "Total Regeneration Capacity", (float) Math.round(regenCapacity * 1000) / 10 + "%  The Ship is Healthy!", false);
        }
        else{
            if(remainingHP < regenCapacity){
                cEngine.maintainStatusForPlayerShip("tbca_biomechanical_hull_capacity", "graphics/icons/hullsys/biomechanical_hull.png", "Total Regeneration Capacity", (float) Math.round(regenCapacity * 1000) / 10 + "% ...Regeneration in Progress!", true);
            }
            else{
                cEngine.maintainStatusForPlayerShip("tbca_biomechanical_hull_capacity", "graphics/icons/hullsys/biomechanical_hull.png", "Total Regeneration Capacity", (float) Math.round(regenCapacity * 1000) / 10 + "% ...Current Regeneration Capacity Reached!", false);
            }
        }

    }
}
