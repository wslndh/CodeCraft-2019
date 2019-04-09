package com.huawei;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.print.attribute.standard.Sides;

import org.apache.log4j.Logger;

import com.manager.StartCars;
import com.manager.forEachRoad;
import com.manager.forEachStation;
import com.util.Condition;
import com.util.CreateCars;
import com.util.CreateRoad;
import com.util.CreateStation;
import com.util.Dijkstra;
import com.util.WriteAnswer;
import com.util.sortStartCars;
import com.util.unLock;
import com.common.Car;
import com.common.Road;
import com.common.Station;
import com.huawei.*;
public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);
	//××××××××××××××××××××××公共变量×××××××××××××××××××××××××××××××××××××××××××××××
	public static int NOWTIME=0;		//当前时间片
	public static int nowOnRoadCars=0;		//记录当前有所少车在路上
	public static int endCarsNumber=0;		//记录当前有多少车已经完成
	public static int NumberEndThisTime=0;		//记录结束这一周车的数目
	public static int MaxNumberCarsOnRoad=0;		//超过这一阈值 将不会执行发车函数
	public static double a,b;//计分系数
	public static long Tpri,TE,TESum=0,Tsumpri=0,Tsum=0;
	public static int priorityLatestEndTime=0;//优先车的最晚完成时间
	public static ArrayList<String> nowOnRoadCarId=new ArrayList<String>();		//目前在路上的车的ID
	public static int nonPriorityAndNonPresetCars=0;
	//×××××××××××××××××各种ID便于获得对应的类×××××××××××××××××××××××××××××××××
	public static ArrayList<String> roadID=new ArrayList<String>();		//路ID
	public static ArrayList<String> nonPriorityCarsId=new ArrayList<String>();		//非优先的车ID
	public static ArrayList<String> priorityCarsId=new ArrayList<String>();		//优先车ID
	public static ArrayList<String> saveCarID=new ArrayList<String>();		//车的ID的副本
	public static ArrayList<String> stationID=new ArrayList<String>();		//路口ID
	//*****************车，路，路口用了map，提高访问速度××××××××××××××××××
	public static Hashtable<String, Road> allRoads=new Hashtable<String, Road>();		//道路ID和道路类map
	public static Hashtable<String, Station> allStations=new Hashtable<String, Station>();
	public static Hashtable<String, Car> allCars=new Hashtable<String, Car>();
	public static ArrayList<Condition> historyConditions=new ArrayList<Condition>();//记录历史地图状况
	public static boolean isLocked=false;
	public static void main(String[] args) throws IOException {
		long startTime=System.currentTimeMillis();   //获取开始时间
		if (args.length != 5) {
		//	logger.error("please input args: inputFilePath, resultFilePath");
			return;
		}
			String carPath = args[0];
	        String roadPath = args[1];
	        String crossPath = args[2];
	        String presetAnswerPath = args[3];
	        String answerPath = args[4];
	       // logger.info("carPath = " + carPath + " roadPath = " + roadPath + 
	        	//	" crossPath = " + crossPath + " presetAnswerPath = " + presetAnswerPath + " and answerPath = " + answerPath);

		//logger.info("start read input files");
		//初始化读取数据
		roadID=CreateRoad.readRoad(roadPath, allRoads);		//创建道路;
		stationID=CreateStation.readStation(crossPath, allStations);		//创建站点 并排序
		CreateCars.readCars(carPath, allCars);		//创建初始车辆
		CreateCars.presetCars(presetAnswerPath,allCars);		//创建预制车辆
		//sortStartCars.sortCarsIdMap(priorityCarsId);//优先车排序
		//sortStartCars.sortCarsIdMap(nonPriorityCarsId);//非优先车排序
		System.out.println("信息读取完成");
		System.out.println("参数a："+a+"  参数b："+b);
		System.out.println("优先车辆："+priorityCarsId.size()+"  非优先车辆："+nonPriorityCarsId.size()+
				"  既不是优先也不预置车辆："+nonPriorityAndNonPresetCars);
		Dijkstra.newGraph();		//新建图
		MaxNumberCarsOnRoad=roadID.size()*13;
		saveCarID.addAll(nonPriorityCarsId);
		saveCarID.addAll(priorityCarsId);
		//两个变量用于判断死否发生死锁 
		int lastNumberEndThisTime;
		int lastnowOnRoadCars;
		int spareTime=0;
		//主循环
		while(endCarsNumber<allCars.size())//当所有车辆都到达站点
		{
			NOWTIME++;//时间片加一
			InitCarState();			//初始化车的状态
			forEachRoad.searchRoad();
			StartCars.startNewCars(true,false,null);		//优先车辆发车
			while(NumberEndThisTime<nowOnRoadCars)		//当所有在路上的车辆都完成本周期
			{
				lastNumberEndThisTime=NumberEndThisTime;
				lastnowOnRoadCars=nowOnRoadCars;
				forEachStation.searchStation();		//再遍历站点
				//如果一个周期内既没有车结束这一周期也没有车进站则发生死锁
				if(isLock(lastNumberEndThisTime,lastnowOnRoadCars))
				{
					isLocked=true;
					unLock.backTrack();//解锁 回朔
					System.gc();
					break;
				}
			}
			if(NumberEndThisTime==nowOnRoadCars)
			{
				isLocked=false;//解锁了
			}
			if(!isLocked)
			{
				System.out.println("已完成："+endCarsNumber+" 优先未发车："+priorityCarsId.size()
				+" 非优先车未发车："+nonPriorityCarsId.size()+" 在路上:"+nowOnRoadCars+" 时间:"+NOWTIME);
				StartCars.startNewCars(true,false,null);		//优先车辆发车
				StartCars.startNewCars(false,false,null);		//非优先新车上路
				copyMapCondition();
			}
		}
		long endTime=System.currentTimeMillis();		 //获取结束时间
		System.out.println("程序运行时间： "+(endTime-startTime)/1000.0+"s");
		calculateScore();
		logger.info("总时间:"+(endTime-startTime)/1000.0+"s\n");
		WriteAnswer.wtiteAnswer(answerPath);
	}
	public static void InitCarState() {
		NumberEndThisTime=0;//所有车都未结束本周期
		Car car=null;
		for(String Id:nowOnRoadCarId)
		{
			car=allCars.get(Id);
			car.setStartThisTime();		//设置车都未结束这一周期
			car.nextChannels=null;		//为车重新选取下一步需要走的方向
			car.nextTurn=null;
		}
		for(String Id:priorityCarsId)
		{
			car=allCars.get(Id);
			car.nowOnChannel=null;
			car.nextChannels=null;		//为车重新选取下一步需要走的方向
			car.nextTurn=null;
		}
		for(String Id:nonPriorityCarsId)
		{
			car=allCars.get(Id);
			car.nowOnChannel=null;
			car.nextChannels=null;		//为车重新选取下一步需要走的方向
			car.nextTurn=null;
		}
	}
	public static boolean isLock(int lastNumberEndThisTime,int lastnowOnRoadCars) {
		if(lastNumberEndThisTime==NumberEndThisTime&&lastnowOnRoadCars==nowOnRoadCars)
		{
			System.out.println("时间"+NOWTIME+"锁死！");
			return true;
		}
		return false;
	}
	public static void calculateScore() {
		Tpri=priorityLatestEndTime-CreateCars.priorityEarlistPlanStartTime;
		TE= Math.round(a*Tpri+NOWTIME);//四舍五入
		TESum= Math.round(b*Tsumpri+Tsum);
		System.out.println("最终调度时间:"+TE);
		System.out.println("最终总调度时间:"+TESum);
		logger.info("最终调度时间:"+TE);
		logger.info("最终总调度时间:"+TESum);
	}
	public static void copyMapCondition() {
		Condition  condition=new Condition();
		condition.endCarsNumber=endCarsNumber;
		condition.isBackTracked=false;
		condition.nonPriorityCarsId.addAll(nonPriorityCarsId);
		condition.priorityCarsId.addAll(priorityCarsId);
		condition.nowOnRoadCarId.addAll(nowOnRoadCarId);
		condition.nowOnRoadCars=nowOnRoadCars;
		condition.copyRoadCondition();
		condition.Tsum=Tsum;
		condition.Tsumpri=Tsumpri;
		condition.priorityLatestEndTime=priorityLatestEndTime;
		condition.copyGraph();
		historyConditions.add(condition);
	}
}