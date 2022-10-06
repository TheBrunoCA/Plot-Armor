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

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if(ship.getVariant().hasHullMod("tbca_biomechanical_hull")){
            return "The Recycle Bots will be eaten by the Bio-Mechanical Hull, giving the latter a bonus in the scarring effect efficiency.";
        }
        return "";
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return minimumArmor * 100f + "%";
        return null;
    }

    Map<ShipAPI.HullSize, Float> recycleSpeed = new HashMap<>();
    {
        recycleSpeed.put(ShipAPI.HullSize.FIGHTER, 1f);
        recycleSpeed.put(ShipAPI.HullSize.FRIGATE, 1f);
        recycleSpeed.put(ShipAPI.HullSize.DESTROYER, 1f);
        recycleSpeed.put(ShipAPI.HullSize.CRUISER, 1f);
        recycleSpeed.put(ShipAPI.HullSize.CAPITAL_SHIP, 1f);
    }
    float minimumArmor = 0.1f;
    float armorRemovalMult = 0.5f;

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

        float totalCells = armorGrid.getGrid().length * armorGrid.getGrid()[0].length;
        float valToRegen = (ship.getMaxHitpoints() * (recycleSpeed.get(ship.getHullSize()) / 100f) * amount) / totalCells;
        boolean isFull = ship.getHullLevel() >= 1f;
        float maxArmor = armorGrid.getMaxArmorInCell();
        boolean repairedSome = false;

        if(!isFull){
            for(int x = 0; x < armorGrid.getGrid().length; x++){
                for(int y = 0; y < armorGrid.getGrid()[0].length; y++){

                    float cellArmor = armorGrid.getArmorValue(x, y);
                    boolean hasMinimumArmor = (cellArmor / maxArmor) > minimumArmor;

                    if(!hasMinimumArmor || isFull){
                        continue;
                    }

                    if(valToRegen > (ship.getMaxHitpoints() - ship.getHitpoints())){
                        valToRegen = ship.getMaxHitpoints() - ship.getHitpoints();
                        isFull = true;
                    }

                    ship.setHitpoints(ship.getHitpoints() + valToRegen);
                    armorGrid.setArmorValue(x, y, cellArmor - (maxArmor * amount * armorRemovalMult));
                    repairedSome = true;

                    if(Objects.equals(ship.getId(), Global.getCombatEngine().getPlayerShip().getId())){
                        Global.getCombatEngine().maintainStatusForPlayerShip(
                                "tbca_recycle_bots_in_action",
                                "graphics/icons/hullsys/recycle_bots.png",
                                "Recycle Bots In Action!",
                                "The Recycle Bots Are Striping The Armor\nAnd Using It To Repair The Hull.\n",
                                false );
                    }
                }
            }
        }
        if(Objects.equals(ship.getId(), Global.getCombatEngine().getPlayerShip().getId()))
            return;

        if(isFull){
            Global.getCombatEngine().maintainStatusForPlayerShip(
                    "tbca_recycle_bots_full_health",
                    "graphics/icons/hullsys/recycle_bots.png",
                    "Recycle Bots In StandBy Mode!",
                    "Hull Is In Perfect Conditions!",
                    false );
        }
        else{
            if(!repairedSome){
                Global.getCombatEngine().maintainStatusForPlayerShip(
                        "tbca_recycle_bots_full_min_armor",
                        "graphics/icons/hullsys/recycle_bots.png",
                        "Recycle Bots Disabled!",
                        "Armor Plates Are In Minimum Condition!",
                        true );
            }
        }

    }
}
