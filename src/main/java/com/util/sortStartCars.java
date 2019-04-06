package com.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.common.Car;
import com.huawei.Main;

public class sortStartCars {
	public static void sortCarsIdMap(ArrayList<String> carsId) {
		Hashtable<Integer, ArrayList<Integer>> carsHashtable=new Hashtable<Integer, ArrayList<Integer>>();
		Car car;
		int speed;
		for(String id:carsId)
		{
			car=Main.allCars.get(id);
			speed=car.isPreSet()?car.getMustStartTime():car.getPlanTime();
			if(carsHashtable.containsKey(speed))
			{
				carsHashtable.get(speed).add(Integer.parseInt(id));
			}
			else {
				ArrayList<Integer> list=new ArrayList<Integer>();
				list.add(Integer.parseInt(id));
				carsHashtable.put(speed, list);
			}
		}
		
		int i=0;
		carsId.clear();
		while(!carsHashtable.isEmpty())
		{
			i++;
			if(carsHashtable.containsKey(i))
			{
				Collections.sort(carsHashtable.get(i));
				for(int id:carsHashtable.get(i))
				{
					carsId.add(String.valueOf(id));
					car =Main.allCars.get(String.valueOf(id));
				}
				carsHashtable.remove(i);
			}
		}
		
	}
}
