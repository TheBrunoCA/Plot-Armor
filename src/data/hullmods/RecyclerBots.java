package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RecyclerBots extends BaseHullMod {

    Map<ShipAPI.HullSize, Float> recycleSpeed = new HashMap<>();
    {
        recycleSpeed.put(ShipAPI.HullSize.FIGHTER, 1f);
        recycleSpeed.put(ShipAPI.HullSize.FRIGATE, 1f);
        recycleSpeed.put(ShipAPI.HullSize.DESTROYER, 1f);
        recycleSpeed.put(ShipAPI.HullSize.CRUISER, 1f);
        recycleSpeed.put(ShipAPI.HullSize.CAPITAL_SHIP, 1f);
    }
    float minimumArmor = 10f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI cEngine = Global.getCombatEngine();

        boolean hasBioHull = ship.getVariant().hasHullMod("tbca_biomechanical_hull");

        if(!ship.isAlive() || hasBioHull){
            if(Objects.equals(ship.getId(), cEngine.getPlayerShip().getId()) && hasBioHull){
                cEngine.maintainStatusForPlayerShip("tbca_recycler_bots_eaten",
                        "graphics/hullsys/recycle_bots.png",
                        "Recycle Bots Eaten!",
                        "The Bio-Mechanical Hull Ate the Recycle Bots\nBio-Mechanical Hull Is Now More Efficient",
                        false);
            }
            return;
        }

        ArmorGridAPI armorGrid = ship.getArmorGrid();



    }
}
