package edu.augustana.csc285.Ibis.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;
import edu.augustana.csc285.Ibis.datamodel.TimePoint;

class AnimalTrackTest {
	AnimalTrack track = new AnimalTrack("chickenbob");
	
	@Test
	void test() {
	assertEquals("chickenbob", track.getAnimalId());
	TimePoint point = new TimePoint(5.0,10.0,25);
	TimePoint point2 = new TimePoint(250,620.0,15);
	track.add(point);
	assertEquals(point,track.getTimePointAtIndex(0));
	assertEquals(point, track.getTimePointAtTime(25));
	track.add(point);
	track.add(point2);
	assertEquals(point, track.getTimePointAtIndex(1));
	assertEquals(3, track.size());
	for(int i = 0; i < track.size(); i++) {
        System.out.println(track.getTimePointAtIndex(i));
    }
	
	}

}
