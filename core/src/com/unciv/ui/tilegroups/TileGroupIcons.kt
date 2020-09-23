package com.unciv.ui.tilegroups

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.unciv.UncivGame
import com.unciv.logic.civilization.CivilizationInfo
import com.unciv.logic.map.MapUnit
import com.unciv.ui.utils.*

/** Helper class for TileGroup, which was getting too full */
class TileGroupIcons(val tileGroup: TileGroup) {

    var improvementIcon: Actor? = null
    var populationIcon: Image? = null //reuse for acquire icon

    var civilianUnitIcon: UnitGroup? = null
    var militaryUnitIcon: UnitGroup? = null
    var aircraftUnitIcon: IconCircleGroup? = null

    fun update(showResourcesAndImprovements: Boolean, showTileYields: Boolean, tileIsViewable: Boolean, showMilitaryUnit: Boolean, viewingCiv: CivilizationInfo?) {
        updateResourceIcon(showResourcesAndImprovements)
        updateImprovementIcon(showResourcesAndImprovements)
        if (viewingCiv != null) updateYieldIcon(showTileYields, viewingCiv)
        updateUnitIcon(tileIsViewable, showMilitaryUnit, viewingCiv)
    }

    fun addPopulationIcon(icon: Image = ImageGetter.getStatIcon("Population")
            .apply { color = Color.GREEN.cpy().lerp(Color.BLACK, 0.5f) }) {
        populationIcon?.remove()
        populationIcon = icon
        populationIcon!!.run {
            setSize(20f, 20f)
            center(tileGroup)
            x += 20 // right
        }
        tileGroup.miscLayerGroup.addActor(populationIcon)
    }

    fun removePopulationIcon() {
        populationIcon?.remove()
        populationIcon = null
    }

    private fun updateUnitIcon(tileIsViewable: Boolean, showMilitaryUnit: Boolean, viewingCiv: CivilizationInfo?) {
        civilianUnitIcon?.remove()
        militaryUnitIcon?.remove()
        aircraftUnitIcon?.remove()
        if (tileGroup.tileInfo.civilianUnit != null && tileIsViewable) {
            civilianUnitIcon = unitIcon(tileGroup.tileInfo.civilianUnit!!, viewingCiv)
            tileGroup.unitLayerGroup.addActor(civilianUnitIcon)
        }
        if (tileGroup.tileInfo.militaryUnit != null && tileIsViewable && showMilitaryUnit) {
            militaryUnitIcon = unitIcon(tileGroup.tileInfo.militaryUnit!!, viewingCiv)
            tileGroup.unitLayerGroup.addActor(militaryUnitIcon)
        }
        if (tileGroup.tileInfo.airUnits.isNotEmpty()) {
            aircraftUnitIcon = aircraftIcon(viewingCiv)
            tileGroup.unitLayerGroup.addActor(aircraftUnitIcon)
        }
    }

    private fun unitIcon(unit: MapUnit, viewingCiv: CivilizationInfo?): UnitGroup {
        return UnitGroup(unit, 25f).apply {
            if (unit.type.isMilitary()) setPosition(15f,35f)
            else setPosition(15f,-5f)
            if (unit.type.isMilitary() && tileGroup.tileInfo.airUnits.isNotEmpty()) x -= 15
            if (!unit.isIdle() && unit.civInfo == viewingCiv) unitBaseImage.color.a = 0.5f
        }
    }

    private fun aircraftIcon(viewingCiv: CivilizationInfo?): IconCircleGroup {
        val aircraftTable = Table()
        val militaryUnit = tileGroup.tileInfo.militaryUnit
        val ownedNation = if (tileGroup.tileInfo.isCityCenter()) tileGroup.tileInfo.getOwner()!!.nation else militaryUnit!!.civInfo.nation
        if (tileGroup.tileInfo.isCityCenter()) {
            aircraftTable.add(ImageGetter.getImage("OtherIcons/Aircraft")
                    .apply { color = ownedNation.getInnerColor() }).size(12f).row()
            aircraftTable.add("${tileGroup.tileInfo.airUnits.size}".toLabel(ownedNation.getInnerColor(), 10))
        }
        else { // when the militaryUnit is not in CityCenter it should be an aircraft carrier
            val unitCapacity = 2 + militaryUnit!!.getUniques().count { it.text == "Can carry 1 extra air unit" }
            aircraftTable.add("${tileGroup.tileInfo.airUnits.size}/$unitCapacity".toLabel(ownedNation.getInnerColor(),10))
        }
        if (tileGroup.tileInfo.airUnits.none { it.canAttack() } && ownedNation == viewingCiv?.nation)
            aircraftTable.color.a = 0.5f
        return aircraftTable.surroundWithCircle(25f,false).apply {
            circle.color = ownedNation.getOuterColor()
            setPosition(15f,35f)
            if (militaryUnit!=null) x += 15f
        }
    }

    fun updateImprovementIcon(showResourcesAndImprovements: Boolean) {
        improvementIcon?.remove()
        improvementIcon = null

        if (tileGroup.tileInfo.improvement != null && showResourcesAndImprovements) {
            val newImprovementImage = ImageGetter.getImprovementIcon(tileGroup.tileInfo.improvement!!)
            tileGroup.miscLayerGroup.addActor(newImprovementImage)
            newImprovementImage.run {
                setSize(20f, 20f)
                center(tileGroup)
                this.x -= 22 // left
                this.y -= 10 // bottom
            }
            improvementIcon = newImprovementImage
        }
        if (improvementIcon != null) {
            improvementIcon!!.color = Color.WHITE.cpy().apply { a = 0.7f }
        }
    }

    // JN updating display of tile yields
    private fun updateYieldIcon(showTileYields: Boolean, viewingCiv: CivilizationInfo) {

        // Hiding yield icons (in order to update)
        tileGroup.tileYieldGroup.isVisible = false


        if (showTileYields) {
            // Setting up YieldGroup Icon
            tileGroup.tileYieldGroup.setStats(tileGroup.tileInfo.getTileStats(viewingCiv))
            tileGroup.tileYieldGroup.setOrigin(Align.center)
            tileGroup.tileYieldGroup.setScale(0.7f)
            tileGroup.tileYieldGroup.toFront()
            tileGroup.tileYieldGroup.centerX(tileGroup)
            tileGroup.tileYieldGroup.y = tileGroup.height * 0.25f - tileGroup.tileYieldGroup.height / 2
            tileGroup.tileYieldGroup.isVisible = true

            // Adding YieldGroup to miscLayerGroup
            tileGroup.miscLayerGroup.addActor(tileGroup.tileYieldGroup)
        }
    }


    fun updateResourceIcon(showResourcesAndImprovements: Boolean) {
        if (tileGroup.resource != tileGroup.tileInfo.resource) {
            tileGroup.resource = tileGroup.tileInfo.resource
            tileGroup.resourceImage?.remove()
            if (tileGroup.resource == null) tileGroup.resourceImage = null
            else {
                val newResourceIcon = ImageGetter.getResourceImage(tileGroup.tileInfo.resource!!, 20f)
                newResourceIcon.center(tileGroup)
                newResourceIcon.x = newResourceIcon.x - 22 // left
                newResourceIcon.y = newResourceIcon.y + 10 // top
                tileGroup.miscLayerGroup.addActor(newResourceIcon)
                tileGroup.resourceImage = newResourceIcon
            }
        }

        if (tileGroup.resourceImage != null) { // This could happen on any turn, since resources need certain techs to reveal them
            val shouldDisplayResource =
                    if (tileGroup.showEntireMap) tileGroup.tileInfo.resource != null
                    else showResourcesAndImprovements
                            && tileGroup.tileInfo.hasViewableResource(UncivGame.Current.worldScreen.viewingCiv)
            tileGroup.resourceImage!!.isVisible = shouldDisplayResource
        }
    }


}