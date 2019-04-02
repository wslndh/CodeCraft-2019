# CodeCraft-2019
2019华为软件精英挑战赛



###############################下一步的计划#####################################
1.加入复赛新增功能后
2.再加入锁死处理机制
3.使用能使车分布更均匀的算法


################################初赛程序结构#####################################
 -类
  --Car类
  --Road类
  --Station类（感觉把Cross路口理解为Station站点更好理解一点）
  --Channel类（车道，有一个carPortList是车辆列表，这样就自动把先来的车排到前面 实现了一个类似于                队列的功能，只有第一个车出路口后面的车才能出路口）
  -主要变量
   --用了三个hashmap存放三个主要的对象
    --Hashtable<String, Road> allRoads
    --Hashtable<String, Station> allStations
    --Hashtable<String, Car> allCars
   --他们可以通过下面三个字符串List进行访问
     --ArrayList<String> roadID 路ID
     --ArrayList<String> carID  车的ID
     --ArrayList<String> stationID 站点ID
 -程序流程
  -读取文件，并存放进hashmap中
  -新建有向图（初始权值是调参的一部分）
  -while（到达终点的车的数量小于车的总数）
    -先初始化在路上的车的状态（因为每一周期当车结束这一周期时，就会置isEndThisTime=true）
    -之后先遍历每条道路 处理不需要通过路口的车
    -再遍历按站点ID升序遍历站点
      --升序遍历站点对应的出路口方向是本站点的路（只遍历正向的）
        --取当前道路第一优先级的车（因为前面不出路口的车已经行驶完毕，因此这个第一优先级的车一定会           出站，即会参与路口优先级排序）
        --如果这个车在本周期内还没有获得过方向就通过迪杰斯特拉获得下一步想要去的方向（一个周期内车           只能获取一次方向，因为前后矛盾的话，路口优先级会乱掉）
        --参与路口优先级排序
  
