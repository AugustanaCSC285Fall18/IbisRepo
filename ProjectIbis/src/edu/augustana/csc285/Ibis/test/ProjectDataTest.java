package edu.augustana.csc285.Ibis.test;

import static org.junit.Assert.assertNotNull;


import java.io.FileNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.opencv.core.Core;

import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;
import edu.augustana.csc285.Ibis.datamodel.ProjectData;
import edu.augustana.csc285.Ibis.datamodel.TimePoint;

class ProjectDataTest {
	
	@BeforeAll
	static void initialize() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
	}

		ProjectData makeFakeData() throws FileNotFoundException {
			ProjectData project =new ProjectData("testVideos/CircleTest1_no_overlap.mp4");
			AnimalTrack track1 = new AnimalTrack("chicken1");
			AnimalTrack track2 = new AnimalTrack("chicken2");
			project.getTracks().add(track1);
			project.getTracks().add(track2);
			track1.add(new TimePoint(100,200,0));
			track1.add(new TimePoint(105,225,30));
			track2.add(new TimePoint(300,400,90));
			return project;
		}
		
		@Test
		void testVideo() throws FileNotFoundException {
			ProjectData project = makeFakeData();
			assertNotNull(project.getVideo());
			project.getTracks().add(new AnimalTrack("chicken4"));
			assertEquals(3,project.getTracks().size());
		}
		
		@Test
		void testTracks() throws FileNotFoundException {
			ProjectData project = makeFakeData();
			assertEquals(90,project.getTracks().get(1).getTimePointAtTime(90).getFrameNum());
			assertEquals(30,project.getTracks().get(0).getTimePointAtIndex(1).getFrameNum());

			/**
			 * 	this is not a test but will help in future how to access data. 
			 *	assertEquals("AnimalTrack[id="+project.getTracks().get(0).getAnimalId()+",numPts=" 
			 *  +project.getTracks().size()+" start="+ project.getVideo().getStartFrameNum() +" end="
			 *	+project.getTracks().get(0).getFinalTimePoint().getFrameNum()+"]", project.getTracks().get(0));
			 */
		}
		
		@Test
		void testJSONSerializationDeserialization() throws FileNotFoundException {
			ProjectData fake = makeFakeData();
			String json = fake.toJSON();
			
			ProjectData reconstructedFake = ProjectData.fromJSON(json);
			
			assertEquals(fake.getVideo().getFilePath(), reconstructedFake.getVideo().getFilePath());		
			assertEquals(fake.getTracks().get(0).getTimePointAtIndex(1), 
					reconstructedFake.getTracks().get(0).getTimePointAtIndex(1));	//need to check this with Stonedahl	
			
		}

	}


