/*
 * Copyright (C) 2013-2018 The enviroCar project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.envirocar.server.rest.decoding.json;

import java.math.BigDecimal;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.envirocar.server.core.dao.SensorDao;
import org.envirocar.server.core.entities.DimensionedNumber;
import org.envirocar.server.core.entities.Fueling;
import org.envirocar.server.rest.JSONConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

/**
 * JSON decoder for {@link Fueling}s.
 *
 * @author Christian Autermann
 */
@Provider
public class FuelingDecoder extends AbstractJSONEntityDecoder<Fueling> {
    public static final boolean MISSED_FUEL_STOP_DEFAULT_VALUE = true;
    private final SensorDao sensorDao;

    /**
     * Creates a new {@code FuelingDecoder}.
     *
     * @param sensorDao the sensor DAO to fetch the sensor
     */
    @Inject
    public FuelingDecoder(SensorDao sensorDao) {
        super(Fueling.class);
        this.sensorDao = sensorDao;
    }

    @Override
    public Fueling decode(JsonNode j, MediaType mt) {
        Fueling fueling = getEntityFactory().createFueling();
        fueling.setFuelType(j.path(JSONConstants.FUEL_TYPE).textValue());
        fueling.setComment(j.path(JSONConstants.COMMENT).textValue());
        fueling.setCost(decodeDimensionedNumber(j.path(JSONConstants.COST)));
        fueling.setMileage(decodeDimensionedNumber(j.path(JSONConstants.MILEAGE)));
        fueling.setMissedFuelStop(j.path(JSONConstants.MISSED_FUEL_STOP).asBoolean(MISSED_FUEL_STOP_DEFAULT_VALUE));
        fueling.setTime(getDateTimeFormat().parseDateTime(j.path(JSONConstants.TIME_KEY).textValue()));
        fueling.setVolume(decodeDimensionedNumber(j.path(JSONConstants.VOLUME)));
        fueling.setCar(sensorDao.getByIdentifier(j.path(JSONConstants.CAR_KEY).textValue()));
        return fueling;
    }

    private DimensionedNumber decodeDimensionedNumber(JsonNode node) {
        BigDecimal value = node.path(JSONConstants.VALUE_KEY).decimalValue();
        String unit = node.path(JSONConstants.UNIT_KEY).textValue();
        return new DimensionedNumber(value, unit);
    }

}
