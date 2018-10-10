package edu.augustana.csc285.Ibis.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.augustana.csc285.Ibis.datamodel.TimePoint;

class TimePointTest {

	@Test
	void test() {
		TimePoint point = new TimePoint(15.0,16.0,335);
		TimePoint point2=new TimePoint (14.0,12.0,255);
		assertEquals(Math.sqrt(17), point.getDistanceTo(point2));
		assertEquals(80, point.compareTo(point2));
	}
	
	@Test
	void testNegativeNumbers() {
		TimePoint point = new TimePoint(-5.0,-5,150);
		TimePoint point2=new TimePoint (10,-5,250);
		assertEquals(Math.sqrt(225), point.getDistanceTo(point2));
		assertEquals(-100, point.compareTo(point2));
		TimePoint point3 = new TimePoint(-5.0,6,-560);
		assertEquals(-560,point3.getFrameNum());
	}
	
	@Test
	void testGets() {
		TimePoint point = new TimePoint(-5.0,-5,150);
		TimePoint point2=new TimePoint (10,-5,250);
		point.setX(10);
		assertEquals(10,point.getX());
		assertEquals(-5,point.getY());
		point2.setY(15);
		assertEquals(15,point2.getY());
		assertEquals(150,point.getFrameNum());

	}

}
