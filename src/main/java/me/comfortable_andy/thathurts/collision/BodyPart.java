package me.comfortable_andy.thathurts.collision;

import org.apache.commons.lang3.Range;

public interface BodyPart {

    double getMultiplier();

    /**
     * @return positive and originates at 0 going left and right, [0, 1] relative to the half size of the x-width of the entity's bounding box
     */
    Range<Double> getXHalfRange();

    /**
     * @return positive and originates at 0 going up, [0, 1] relative to the height of the entity
     */
    Range<Double> getYRange();

    /**
     * @return positive and originates at 0 going forward and backward, [0, 1] relative to the half size of the z-width of the entity's bounding box
     */
    Range<Double> getZHalfRange();

}
