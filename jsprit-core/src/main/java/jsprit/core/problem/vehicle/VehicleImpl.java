/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.vehicle;

import jsprit.core.problem.AbstractVehicle;
import jsprit.core.problem.Skills;
import jsprit.core.util.Coordinate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Implementation of {@link Vehicle}.
 * 
 * @author stefan schroeder
 * 
 */

public class VehicleImpl extends AbstractVehicle{



    /**
	 * Extension of {@link VehicleImpl} representing an unspecified vehicle with the id 'noVehicle'
	 * (to avoid null).
	 * 
	 * @author schroeder
	 *
	 */
	public static class NoVehicle extends VehicleImpl {
		
		public NoVehicle() {
			super(Builder.newInstance("noVehicle").setType(VehicleTypeImpl.Builder.newInstance("noType").build()));
		}
		
	}
	
	/**
	 * Builder that builds the vehicle.
	 * 
	 * <p>By default, earliestDepartureTime is 0.0, latestDepartureTime is Double.MAX_VALUE,
	 * it returns to the depot and its {@link VehicleType} is the DefaultType with typeId equal to 'default'
	 * and a capacity of 0.
	 * 
	 * @author stefan
	 *
	 */
	public static class Builder {

        static final Logger log = LogManager.getLogger(Builder.class.getName());

        private String id;
		
		private String locationId;

		private Coordinate locationCoord;

		private double earliestStart = 0.0;

		private double latestArrival = Double.MAX_VALUE;
		
		private String startLocationId;

		private Coordinate startLocationCoord;
		
		private String endLocationId;

		private Coordinate endLocationCoord;
		
		private boolean returnToDepot = true;
		
		private VehicleType type = VehicleTypeImpl.Builder.newInstance("default").build();

        private Skills.Builder skillBuilder = Skills.Builder.newInstance();

        private Skills skills;

        private Builder(String id) {
			super();
			this.id = id;
		}
		
		/**
		 * Sets the {@link VehicleType}.<br>
		 * 
		 * @param type the type to be set
		 * @throws IllegalStateException if type is null
		 * @return this builder
		 */
		public Builder setType(VehicleType type){
			if(type==null) throw new IllegalStateException("type cannot be null.");
			this.type = type;
			return this;
		}
		
		/**
		 * Sets the flag whether the vehicle must return to depot or not.
		 * 
		 * <p>If returnToDepot is true, the vehicle must return to specified end-location. If you
		 * omit specifying the end-location, vehicle returns to start-location (that must to be set). If
		 * you specify it, it returns to specified end-location.
		 * 
		 * <p>If returnToDepot is false, the end-location of the vehicle is endogenous.
		 * 
		 * @param returnToDepot true if vehicle need to return to depot, otherwise false
		 * @return this builder
		 */
		public Builder setReturnToDepot(boolean returnToDepot){
			this.returnToDepot = returnToDepot;
			return this;
		}
		
		/**
		 * Sets the start-location of this vehicle.
		 * 
		 * @param startLocationId the location id of vehicle's start
		 * @return this builder
		 * @throws IllegalArgumentException if startLocationId is null
		 */
		public Builder setStartLocationId(String startLocationId){
			if(startLocationId == null) throw new IllegalArgumentException("startLocationId cannot be null");
			this.startLocationId = startLocationId;
			this.locationId = startLocationId;
			return this;
		}
		
		/**
		 * Sets the start-coordinate of this vehicle.
		 * 
		 * @param coord the coordinate of vehicle's start location
		 * @return this builder
		 */
		public Builder setStartLocationCoordinate(Coordinate coord){
			this.startLocationCoord = coord;
			this.locationCoord = coord;
			return this;
		}
		
		/**
		 * Sets the end-locationId of this vehicle.
		 * 
		 * @param endLocationId the location id of vehicle's end
		 * @return this builder
		 */
		public Builder setEndLocationId(String endLocationId){
			this.endLocationId = endLocationId;
			return this;
		}
		
		/**
		 * Sets the end-coordinate of this vehicle.
		 * 
		 * @param coord the coordinate of vehicle's end location
		 * @return this builder
		 */
		public Builder setEndLocationCoordinate(Coordinate coord){
			this.endLocationCoord = coord;
			return this;
		}
		
		/**
		 * Sets earliest-start of vehicle which should be the lower bound of the vehicle's departure times.
		 * 
		 * @param earliest_startTime the earliest start time / departure time of the vehicle at its start location
		 * @return this builder
		 */
		public Builder setEarliestStart(double earliest_startTime){
			this.earliestStart = earliest_startTime;
			return this;
		}
		
		/**
		 * Sets the latest arrival at vehicle's end-location which is the upper bound of the vehicle's arrival times.
		 * 
		 * @param latest_arrTime the latest arrival time of the vehicle at its end location
		 * @return this builder
		 */
		public Builder setLatestArrival(double latest_arrTime){
			this.latestArrival = latest_arrTime;
			return this;
		}

        public Builder addSkill(String skill){
            skillBuilder.addSkill(skill);
            return this;
        }
		
		/**
		 * Builds and returns the vehicle.
		 * 
		 * <p>if {@link VehicleType} is not set, default vehicle-type is set with id="default" and 
		 * capacity=0
		 * 
		 * <p>if startLocationId || locationId is null (=> startLocationCoordinate || locationCoordinate must be set) then startLocationId=startLocationCoordinate.toString() 
		 * and locationId=locationCoordinate.toString() [coord.toString() --> [x=x_val][y=y_val])
		 * <p>if endLocationId is null and endLocationCoordinate is set then endLocationId=endLocationCoordinate.toString()
		 * <p>if endLocationId==null AND endLocationCoordinate==null then endLocationId=startLocationId AND endLocationCoord=startLocationCoord
		 * Thus endLocationId can never be null even returnToDepot is false.
		 * 
		 * @return vehicle
		 * @throws IllegalStateException if both locationId and locationCoord is not set or (endLocationCoord!=null AND returnToDepot=false) 
		 * or (endLocationId!=null AND returnToDepot=false)  
		 */
		public VehicleImpl build(){
			if((locationId == null && locationCoord == null) && (startLocationId == null && startLocationCoord == null)){
				throw new IllegalStateException("vehicle requires startLocation. but neither locationId nor locationCoord nor startLocationId nor startLocationCoord has been set");
			}
			if(locationId == null && locationCoord != null) {
				locationId = locationCoord.toString();
				startLocationId = locationCoord.toString();
			}
			if(locationId == null && locationCoord == null) throw new IllegalStateException("locationId and locationCoord is missing.");
			if(locationCoord == null) log.warn("locationCoord for vehicle " + id + " is missing.");
			if(endLocationId == null && endLocationCoord != null) endLocationId = endLocationCoord.toString();
			if(endLocationId == null && endLocationCoord == null) {
				endLocationId = startLocationId;
				endLocationCoord = startLocationCoord;
			}
			if( !startLocationId.equals(endLocationId) && !returnToDepot) throw new IllegalStateException("this must not be. you specified both endLocationId and open-routes. this is contradictory. <br>" +
					"if you set endLocation, returnToDepot must be true. if returnToDepot is false, endLocationCoord must not be specified.");
			skills = skillBuilder.build();
            return new VehicleImpl(this);
		}
		
		/**
		 * Returns new instance of vehicle builder.
		 * 
		 * @param vehicleId the id of the vehicle which must be a unique identifier among all vehicles
		 * @return vehicle builder
		 */
		public static Builder newInstance(String vehicleId){ return new Builder(vehicleId); }
		
	}

	/**
	 * Returns empty/noVehicle which is a vehicle having no capacity, no type and no reasonable id.
	 * 
	 * <p>NoVehicle has id="noVehicle" and extends {@link VehicleImpl}
	 * 
	 * @return emptyVehicle
	 */
	public static NoVehicle createNoVehicle(){
		return new NoVehicle();
	}
	
	private final String id;

	private final VehicleType type;

	private final String locationId;

	private final Coordinate coord;

	private final double earliestDeparture;

	private final double latestArrival;
	
	private final boolean returnToDepot;

	private final Coordinate endLocationCoord;

	private final String endLocationId;

	private final Coordinate startLocationCoord;

	private final String startLocationId;

    private Skills skills;

	private VehicleImpl(Builder builder){
		id = builder.id;
		type = builder.type;
		coord = builder.locationCoord;
		locationId = builder.locationId;
		earliestDeparture = builder.earliestStart;
		latestArrival = builder.latestArrival;
		returnToDepot = builder.returnToDepot;
		startLocationId = builder.startLocationId;
		startLocationCoord = builder.startLocationCoord;
		endLocationId = builder.endLocationId;
		endLocationCoord = builder.endLocationCoord;
        setVehicleIdentifier(new VehicleTypeKey(type.getTypeId(),startLocationId,endLocationId,earliestDeparture,latestArrival));
        skills = builder.skills;
	}
	
	/**
	 * Returns String with attributes of this vehicle
	 * 
	 * <p>String has the following format [attr1=val1][attr2=val2]...[attrn=valn]
	 */
	@Override
	public String toString() {
		return "[id="+id+"][type="+type+"][locationId="+locationId+"][coord=" + coord + "][isReturnToDepot=" + isReturnToDepot() + "]";
	}

	@Override
	public double getEarliestDeparture() {
		return earliestDeparture;
	}

	@Override
	public double getLatestArrival() {
		return latestArrival;
	}

	@Override
	public VehicleType getType() {
		return type;
	}

	@Override
	public String getId() {
		return id;
	}

	public boolean isReturnToDepot() {
		return returnToDepot;
	}

	@Override
	public String getStartLocationId() {
		return this.startLocationId;
	}

	@Override
	public Coordinate getStartLocationCoordinate() {
		return this.startLocationCoord;
	}

	@Override
	public String getEndLocationId() {
		return this.endLocationId;
	}

	@Override
	public Coordinate getEndLocationCoordinate() {
		return this.endLocationCoord;
	}

    @Override
    public Skills getSkills() {
        return skills;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * Two vehicles are equal if they have the same id and if their types are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VehicleImpl other = (VehicleImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	
	
	
}
