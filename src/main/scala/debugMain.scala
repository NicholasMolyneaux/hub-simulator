import hubmodel.Position
import hubmodel.io.output.image.DrawCellsAndWalls
import hubmodel.route.Guo2011.HexagonPotentialField
import hubmodel.supply.continuous.ReadContinuousSpace
import myscala.math.vector.Vector2D
import myscala.timeBlock


/*
  val qInterval1 = computeQuantiles(0.0 to 100.0 by 1.0)_
  val qInterval10 = computeQuantiles(0.0 to 100.0 by 10.0)_

  val travelTimeByOD = aggregateQuantityByOD(mvmt.getPed, "travelTime")

  val travelTimeQuantilesWithTime: Vector[ODQuantiles] = getQuantiles(travelTimeByOD, qInterval10).map(
    q => ODQuantiles(string2LocalDateTime("1970-01-01 00:00:00"), string2LocalDateTime("2100-01-01 00:00:00"), nameMappings.int2StringMap(q._1._1), nameMappings.int2StringMap(q._1._2), q._2)
  ).toVector

  travelTimeQuantilesWithTime.writeToJSON(nameMappings, "test.json")
*/

object debugMain extends App {

  /*case class MyCell(center: Position, edgeLength: Double){ //}, conn: List[String]) {
    val ID: String = generateUUID
    val A: Position = center + edgeLength*DenseVector(-cos(30*math.Pi/180.0), sqrt(1-pow(cos(30*math.Pi/180.0),2)))
    val B: Position = A + edgeLength*DenseVector(0.0,-1.0)
    val C: Position = B + edgeLength*DenseVector(cos(30.0*math.Pi/180.0), -sqrt(1.0-pow(cos(30.0*math.Pi/180.0),2)))
    val D: Position = C + edgeLength*DenseVector(cos(30.0*math.Pi/180.0), sqrt(1.0-pow(cos(30.0*math.Pi/180.0),2)))
    val E: Position = D + edgeLength*DenseVector(0.0,1.0)
    val F: Position = E + edgeLength*DenseVector(-cos(30.0*math.Pi/180.0), sqrt(1.0-pow(cos(30.0*math.Pi/180.0),2)))

    val angles: List[Position] = List(A,B,C,D,E,F)

    var pedAcc: Double = 0.0
    var potential: Double = 0.0
    var stepsToFinal: Int = 0
    var updateState: Int = 0


    def isInside(p: Position): Boolean = {
      // https://stackoverflow.com/questions/5193331/is-a-point-inside-regular-hexagon
      val d: Double = breeze.linalg.norm(p - center)

      if (d > edgeLength) return false
      else if (d <= edgeLength*cos(30.0*math.Pi/180.0)) return true

      val px: Double = (p(0)-center(0)) * 2/sqrt(3)
      if (px > 1.0 || px < -1.0) return false

      val py: Double = 0.5 * px + (p(1) - center(1))
      if (py < 1.0 || py < -1.0 ) false
      else if (px-py < 1.0 || px-py < -1.0) false
      else true
    }

    def xCoords: Array[Double] = Array(A(0), B(0), C(0), D(0), E(0), F(0))
    def yCoords: Array[Double] = Array(A(1), B(1), C(1), D(1), E(1), F(1))

  }

  object MyVertex {
    implicit def orderingByPotential[A <: MyCell]: Ordering[A] = Ordering.by(v => v.potential)
  }
  */

  /*case class MyConnection(v: MyCell, c: List[MyCell])
  object MyConnection {
    implicit def orderingByPotential[A <: MyConnection]: Ordering[A] = Ordering.by(c => c.v.potential)
  }*/


  /*val a = MyVertex("a", DenseVector(0.0,0.0), 2.0)//, List("b","c","d"))
  val b = MyVertex("b", DenseVector(0.0,0.0), 2.0)//, List("a","e"))
  val c = MyVertex("c", DenseVector(0.0,0.0), 2.0)//, List("a","d","h","f","g"))
  val d = MyVertex("d", DenseVector(0.0,0.0), 2.0)//, List("a","i","c","h"))
  val e = MyVertex("e", DenseVector(0.0,0.0), 2.0)//, List("b"))
  val f = MyVertex("f", DenseVector(0.0,0.0), 2.0)//, List("c","g"))
  val g = MyVertex("g", DenseVector(0.0,0.0), 2.0)//, List("c","f"))
  val h = MyVertex("h", DenseVector(0.0,0.0), 2.0)//, List("i","d","c"))
  val i = MyVertex("i", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val j = MyVertex("j", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val k = MyVertex("k", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val l = MyVertex("l", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val m = MyVertex("m", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val n = MyVertex("n", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val o = MyVertex("o", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val p = MyVertex("p", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val q = MyVertex("q", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val r = MyVertex("r", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val s = MyVertex("s", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val t = MyVertex("t", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val u = MyVertex("u", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val v = MyVertex("v", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val w = MyVertex("w", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val x = MyVertex("x", DenseVector(0.0,0.0), 2.0)//, List("d","h"))
  val destination = MyVertex("dest", DenseVector(0.0,0.0), 2.0)

  f.pedAcc=3.0
  g.pedAcc=3.0
  c.pedAcc=3.0
  r.pedAcc=4.0
  s.pedAcc=2.0
  a.pedAcc=5.0


  val connections: List[MyConnection] = List(
   MyConnection(a,List(b,c,d)),
    MyConnection(b, List(a,e)),
    MyConnection(c, List(a,d,h,f,g)),
    MyConnection(d, List(a,e,i,c,h)),
    MyConnection(e, List(b)),
    MyConnection(f, List(c,g)),
    MyConnection(g, List(c,f)),
    MyConnection(h, List(i,d,c)),
    MyConnection(i, List(d,h))
  )

  val connections2: Map[MyVertex, List[MyVertex]] = Map(
    a -> List(b,c,d),
    b -> List(a,e),
    c -> List(a,d,h,f,g),
    d -> List(a,e,i,c,h),
    e -> List(b,x),
    f -> List(c,g,r),
    g -> List(c,f,r),
    h -> List(i,d,c),
    i -> List(d,h,j,k),
    j -> List(h,i,k,l),
    k -> List(i,j,l),
    l -> List(j,k,m),
    m -> List(l,n),
    n -> List(m,o),
    o -> List(n,p),
    p -> List(o,q),
    q -> List(p,r,s),
    r -> List(f,g,s,q),
    s -> List(q,r,t),
    t -> List(s,u),
    u -> List(t,v),
    v -> List(u,w),
    w -> List(x,v),
    x -> List(e,w)
  )
  */
  def buildGraph(conn: (HexagonPotentialField, List[HexagonPotentialField]), connections: Map[HexagonPotentialField, List[HexagonPotentialField]], acc: List[(HexagonPotentialField, HexagonPotentialField)]): List[(HexagonPotentialField, HexagonPotentialField)] = {
    if (connections.isEmpty) acc ++ conn._2.filter(_.potential >= conn._1.potential).map((_, conn._1))
    else buildGraph(connections.head, connections.tail, conn._2.filter(_.potential >= conn._1.potential).map((_, conn._1)) ++ acc)
  }

  val infraSF = new ReadContinuousSpace("piw-corridor/walls.json")

  def insideSpace: Position => Boolean = infraSF.continuousSpace.isInsideWalkableArea

  //  def insideSpace: Position =>  Boolean = i => true


  val xMin = infraSF.continuousSpace.walls.map(w => Math.min(w.startPoint.X, w.endPoint.X)).min
  val xMax = infraSF.continuousSpace.walls.map(w => Math.max(w.startPoint.X, w.endPoint.X)).max
  val yMin = infraSF.continuousSpace.walls.map(w => Math.min(w.startPoint.Y, w.endPoint.Y)).min
  val yMax = infraSF.continuousSpace.walls.map(w => Math.max(w.startPoint.Y, w.endPoint.Y)).max
  val radius: Double = 1.5


  /*
    def insideSpace(p: Position): Boolean = {

      val xMin1 = 0.0
      val xMax1 = 10.0
      val yMin1 = 0.0
      val yMax1 = 7.0


      val xMin2 = 0.0
      val xMax2 = 100.0
      val yMin2 = 7.0
      val yMax2 = 13.0

      (p.X >= xMin1 && p.X <= xMax1 && p.Y >= yMin1 && p.Y <= yMax1) || (p.X >= xMin2 && p.X <= xMax2 && p.Y >= yMin2 && p.Y <= yMax2)
    }*/


  val hexagons: IndexedSeq[HexagonPotentialField] = (for (
    x <- xMin to (xMax + radius) by 2 * radius * cos(30.0 * math.Pi / 180.0);
    y <- yMin to (yMax + radius) by 3 * radius)
    yield {
      new HexagonPotentialField(Vector2D(x, y), radius)
    }).filter(h => h.corners.exists(insideSpace)) ++ (for (
    x <- (xMin + radius * cos(30.0 * math.Pi / 180.0)) to (xMax + radius) by 2 * radius * cos(30.0 * math.Pi / 180.0);
    y <- yMin + 1.5 * radius to (yMax + radius) by 3 * radius)
    yield {
      new HexagonPotentialField(Vector2D(x, y), radius)
    }).filter(h => h.corners.exists(insideSpace))


  val connections2: Map[HexagonPotentialField, List[HexagonPotentialField]] = hexagons.map(h => h -> hexagons.filter(hin => (h.center - hin.center).norm < 1.01 * 2 * radius * cos(30.0 * math.Pi / 180.0)).filterNot(h == _).toList).toMap


  val doorwayPoints = (9.0 to 11.0 by 0.25).map(y => DenseVector(0.0, y))

  val finalCells: IndexedSeq[HexagonPotentialField] = hexagons.filter(h => doorwayPoints.exists(p => h.isInside(Vector2D(p(0), p(1)))))


  /*
  // static algortihm

  // block 1
  finalCells.foreach(_.stepsToFinal=1)
  var l: Int = 1
  while (connections.exists(_.v.stepsToFinal==0)){
    connections.filter(_.v.stepsToFinal == l).foreach(_.c.filter(c => c.stepsToFinal==0).foreach(c => c.stepsToFinal=l+1))
    l = l + 1
  }

  // block 2
  val lMax: Int = connections.map(_.v.stepsToFinal).max
  val theta: Double = 0.8
  connections.filter(_.v.stepsToFinal==1).foreach(_.v.potential=1.0)
  l = 2
  while(l <= lMax) {
    connections.filter(_.v.stepsToFinal == l).foreach(p => {
      val setCells: List[MyVertex] = p.c.filter(c => c.potential >= 0.0 && c.stepsToFinal == l - 1)
      if (setCells.size == 1) {
        p.v.potential = setCells.head.potential + 1.0
      }
      else {
        p.v.potential = theta + setCells.map(_.potential).sum / setCells.size.toDouble
      }
    })
    l = l + 1
  }

  println(connections.map(_.v.potential))
  */

  // dynamic algorithm
  val tau: Double = 0.1
  val theta: Double = 0.9
  var mCounter = finalCells.length
  //var l = 1
  finalCells.foreach(v => {
    v.updateState = 1;
    v.potential = 1
  })
  var lCounter: Int = 1
  while (mCounter != 0) {
    val V = connections2.filter(v => v._1.updateState == 1 && v._1.potential <= lCounter)
    V.foreach(_._1.updateState = 2)
    mCounter = mCounter - V.size
    V.foreach(conn => {
      conn._2.filter(_.updateState == 0).foreach(j => {
        j.updateState = 1
        mCounter = mCounter + 1
        val psi: Int = connections2(j).count(_.updateState == 2)
        if (psi == 1) {
          j.potential = conn._1.potential + 1 + tau * j.pedAcc
        }
        else {
          j.potential = connections2(j).filter(_.updateState == 2).map(_.potential).sum / psi + theta + tau * j.pedAcc
        }
      })
      lCounter = lCounter + 1
    })
  }

  //println(lCounter, connections2.map(conn => conn._1.ID + " pot=" + conn._1.potential).mkString("\n"))
  //println(u.potential,v.potential,w.potential,x.potential,e.potential,b.potential,a.potential)
  //println(u.potential,t.potential,s.potential,r.potential,f.potential,c.potential,a.potential)

  val destination = new HexagonPotentialField(Vector2D(0.0, 0.0), radius)
  val conn3: Map[HexagonPotentialField, List[HexagonPotentialField]] = connections2 + (destination -> finalCells.toList)


  val g = timeBlock {
    buildGraph(conn3.head, conn3.tail, List())
  }

  //println(g)
  //println(hexagons.map(_.potential).mkString("\n"))

  new DrawCellsAndWalls(hexagons, infraSF.continuousSpace.walls, "celltest.png")

}


