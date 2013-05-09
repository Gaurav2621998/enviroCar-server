/**
 * Copyright (C) 2013  Christian Autermann, Jan Alexander Wirwahn,
 *                     Arne De Wall, Dustin Demuth, Saqib Rasheed
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
package io.car.server.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.car.server.core.db.GroupDao;
import io.car.server.core.db.MeasurementDao;
import io.car.server.core.db.PhenomenonDao;
import io.car.server.core.db.SensorDao;
import io.car.server.core.db.TrackDao;
import io.car.server.core.db.UserDao;
import io.car.server.core.entities.Group;
import io.car.server.core.entities.Groups;
import io.car.server.core.entities.Measurement;
import io.car.server.core.entities.Measurements;
import io.car.server.core.entities.Phenomenon;
import io.car.server.core.entities.Phenomenons;
import io.car.server.core.entities.Sensor;
import io.car.server.core.entities.Sensors;
import io.car.server.core.entities.Track;
import io.car.server.core.entities.Tracks;
import io.car.server.core.entities.User;
import io.car.server.core.entities.Users;
import io.car.server.core.exception.GroupNotFoundException;
import io.car.server.core.exception.IllegalModificationException;
import io.car.server.core.exception.PhenomenonNotFoundException;
import io.car.server.core.exception.ResourceAlreadyExistException;
import io.car.server.core.exception.SensorNotFoundException;
import io.car.server.core.exception.TrackNotFoundException;
import io.car.server.core.exception.UserNotFoundException;
import io.car.server.core.exception.ValidationException;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author Arne de Wall <a.dewall@52north.org>
 */
@Singleton
public class Service {
    @Inject
    private UserDao userDao;
    @Inject
    private GroupDao groupDao;
    @Inject
    private TrackDao trackDao;
    @Inject
    private MeasurementDao measurementDao;
    @Inject
    private SensorDao sensorDao;
    @Inject
    private PhenomenonDao phenomenonDao;
    @Inject
    private EntityValidator<User> userValidator;
    @Inject
    private EntityValidator<Group> groupValidator;
    @Inject
    private EntityValidator<Track> trackValidator;
    @Inject
    private EntityUpdater<Track> trackUpdater;
    @Inject
    private EntityUpdater<Group> groupUpdater;
    @Inject
    private EntityUpdater<User> userUpdater;
    @Inject
    private EntityUpdater<Measurement> measurementUpdater;
    @Inject
    private EntityValidator<Measurement> measurementValidator;
    @Inject
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) throws ValidationException,
                                             ResourceAlreadyExistException {
        userValidator.validateCreate(user);
        if (userDao.getByName(user.getName()) != null) {
            throw new ResourceAlreadyExistException();
        }
        user.setToken(passwordEncoder.encode(user.getToken()));
        return this.userDao.create(user);
    }

    public User getUser(String name) throws UserNotFoundException {
        User user = this.userDao.getByName(name);
        if (user == null) {
            throw new UserNotFoundException(name);
        }
        return user;
    }

    public Users getAllUsers(int limit) {
        return this.userDao.getAll(limit);
    }

    public Users getAllUsers() {
        return getAllUsers(0);
    }

    public User modifyUser(User user, User changes) throws UserNotFoundException, IllegalModificationException,
                                                           ValidationException {
        return this.userDao.save(this.userUpdater.update(userValidator.validateUpdate(user), user));
    }

    public void deleteUser(String username) throws UserNotFoundException {
        deleteUser(getUser(username));
    }

    public void deleteUser(User user) throws UserNotFoundException {
        this.userDao.delete(user);
    }

    public void removeFriend(User user, User friend) throws UserNotFoundException {
        this.userDao.save(user.removeFriend(getUser(friend.getName())));
    }

    public void addFriend(User user, User friend) throws UserNotFoundException {
        this.userDao.save(user.addFriend(getUser(friend.getName())));
    }

    public Group getGroup(String name) throws GroupNotFoundException {
        Group group = this.groupDao.getByName(name);
        if (group == null) {
            throw new GroupNotFoundException(name);
        }
        return group;
    }

    public Groups getAllGroups(int limit) {
        return this.groupDao.getAll(limit);
    }

    public Group createGroup(Group group) throws ValidationException,
                                                 ResourceAlreadyExistException {
        groupValidator.validateCreate(group);
        if (groupDao.getByName(group.getName()) != null) {
            throw new ResourceAlreadyExistException();
        }
        return this.groupDao.create(group);
    }

    public Group modifyGroup(Group group, Group changes) throws ValidationException, IllegalModificationException {
        groupValidator.validateUpdate(group);
        return this.groupDao.save(this.groupUpdater.update(changes, group));
    }

    public Track modifyTrack(Track track, Track changes) throws ValidationException, IllegalModificationException {
        trackValidator.validateCreate(track);
        return this.trackDao.save(this.trackUpdater.update(changes, track));
    }

    public void deleteGroup(String username) throws GroupNotFoundException {
        deleteGroup(getGroup(username));
    }

    public void deleteGroup(Group group) throws GroupNotFoundException {
        this.groupDao.delete(group);
    }

    public Groups getGroupsOfUser(User user, int limit) {
        return this.groupDao.getByMember(user);
    }

    public Groups searchGroups(String search, int limit) {
        return this.groupDao.search(search, limit);
    }

    public void addGroupMember(Group group, User user)
            throws UserNotFoundException {
        this.groupDao.save(group.addMember(getUser(user.getName())));
    }

    public void removeGroupMember(Group group, User user)
            throws UserNotFoundException {
        this.groupDao.save(group.removeMember(getUser(user.getName())));
    }

    // track stuff //
    public Tracks getAllTracks() {
        return getAllTracks(0);
    }

    public Tracks getAllTracks(int limit) {
        return trackDao.getAll(limit);
    }

    public Track getTrack(String id) throws TrackNotFoundException {
        Track track = trackDao.getById(id);
        if (track == null) {
            throw new TrackNotFoundException(id);
        }
        return track;
    }

    public Track createTrack(Track track) throws ValidationException {
        return this.trackDao.create(this.trackValidator.validateCreate(track));
    }

    public Measurements getAllMeasurements(int limit) {
        return this.measurementDao.getAll(limit);
    }

    public Measurement getMeasurement(String id) {
        return this.measurementDao.getById(id);
    }

    public void deleteTrack(String id) throws TrackNotFoundException {
        this.trackDao.delete(getTrack(id));
    }

    public void deleteTrack(Track track) {
        this.trackDao.delete(track);
    }

    public Track addMeasurement(Track track, Measurement measurement) throws ValidationException {
        return trackDao
                .save(track.addMeasurement(measurementDao.save(measurementValidator.validateCreate(measurement))));
    }

    public Phenomenon getPhenomenonByName(String name) throws PhenomenonNotFoundException {
        Phenomenon p = this.phenomenonDao.getByName(name);
        if (p == null) {
            throw new PhenomenonNotFoundException(name);
        }
        return p;
    }

    public Sensor getSensorByName(String name) throws SensorNotFoundException {
        Sensor s = this.sensorDao.getByName(name);
        if (s == null) {
            throw new SensorNotFoundException(name);
        }
        return s;
    }

    public Phenomenons getAllPhenomenons() {
        return this.phenomenonDao.getAll();
    }

    public Sensors getAllSensors() {
        return this.sensorDao.getAll();
    }
}