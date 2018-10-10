package edu.augustana.csc285.Ibis.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;
import edu.augustana.csc285.Ibis.datamodel.TimePoint;

class AnimalTrackTest {
	
	@Test
	void test() {
	AnimalTrack track = new AnimalTrack("chickenbob");
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
	
	@Test 
	void testbinarySearch() {
		AnimalTrack track2 = new AnimalTrack("chickensam");
		TimePoint point1 = new TimePoint(45,35,-2);
		TimePoint point2 = new TimePoint(45,35,0);
		TimePoint point3 = new TimePoint(45,35,6);
		TimePoint point4 = new TimePoint(45,35,50);
		TimePoint point5 = new TimePoint(45,45,52);
		TimePoint point6 = new TimePoint(45,35,56);
		track2.add(point1);
		track2.add(point2);
		track2.add(point3);
		track2.add(point4);
		track2.add(point5);
		track2.add(point6);
		assertEquals(point1, track2.getTimePointAtTime(-2));
		assertEquals(point2, track2.getTimePointAtTime(0));
		assertEquals(point3, track2.getTimePointAtTime(6));
		assertEquals(point4, track2.getTimePointAtTime(50));
		assertEquals(point5, track2.getTimePointAtTime(52));
		assertEquals(point6, track2.getTimePointAtTime(56));
		assertEquals(null, track2.getTimePointAtTime(55));
	}

}
