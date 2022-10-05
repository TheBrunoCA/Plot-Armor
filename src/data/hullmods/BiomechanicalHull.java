package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.util.HashMap;
import java.util.Map;

public class BiomechanicalHull extends BaseHullMod {

    final Map<ShipAPI.HullSize, Float> regenSpeed = new HashMap<>();
    {
        regenSpeed.put(ShipAPI.HullSize.FIGHTER, 1f);
        regenSpeed.put(ShipAPI.HullSize.FRIGATE, 1f);
        regenSpeed.put(ShipAPI.HullSize.DESTROYER, 1f);
        regenSpeed.put(ShipAPI.HullSize.CRUISER, 1f);
        regenSpeed.put(ShipAPI.HullSize.CAPITAL_SHIP, 1f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(!ship.isAlive()) return;
        CombatEngineAPI cEngine = Global.getCombatEngine();

        Map<String, Object> customData = cEngine.getCustomData();
        String capacityKey = "tbca_biomechanical_hull_capacity"+ship.getId();
        String alreadyRegenKey = "tbca_biomechanical_hull_alreadyRegen"+ship.getId();

        if(customData.get(capacityKey) == null){
            customData.put(capacityKey, 100f);
        }
        if(customData.get(alreadyRegenKey) == null){
            customData.put(capacityKey, 0f);
        }

        float regenCapacity = (float)customData.get(capacityKey);
        float alreadyRegen = (float)customData.get(alreadyRegenKey);

        float remainingHP = ship.getHitpoints() / ship.getMaxHitpoints() * 100f;

        if(remainingHP >= regenCapacity)
            return;

        float valToRegen = ship.getMaxHitpoints() * (regenSpeed.get(ship.getHullSize())) * amount;

        if(valToRegen > ship.getMaxHitpoints() * regenCapacity - ship.getHitpoints())
            valToRegen = ship.getMaxHitpoints() * regenCapacity - ship.getHitpoints();

        alreadyRegen += valToRegen / ship.getMaxHitpoints() * 100f;

        regenCapacity = 100f - alreadyRegen / 3f;

        float val = ship.getHitpoints() + valToRegen;

        ship.setHitpoints(val);

        customData.put(capacityKey, regenCapacity);
        customData.put(alreadyRegenKey, alreadyRegen);

        if(ship != cEngine.getPlayerShip())
            return;

        cEngine.maintainStatusForPlayerShip("tbca_biomechanical_hull_capacity", "graphics/icons/hullsys/biomechanical_hull.png", "Total Regeneration Capacity", (float) Math.round(regenCapacity * 1000) / 10 + "%", true);
    }
}
