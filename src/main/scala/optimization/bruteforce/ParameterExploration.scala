package optimization.bruteforce

import java.io.File

import com.typesafe.config.Config
import hubmodel.DES.SFGraphSimulator
import hubmodel.mgmt.ControlDevices
import hubmodel.ped.PedestrianSim
import hubmodel.{ResultsContainerNew, createSimulation, runAndWriteResults}
import hubmodel.supply.graph.FlowGateFunctional
import hubmodel.tools.cells.DensityMeasuredArea
import myscala.math.stats.ComputeStats
import trackingdataanalysis.visualization.HeatMap

import scala.collection.immutable.NumericRange
import scala.collection.parallel.ForkJoinTaskSupport
import myscala.output.SeqTuplesExtensions.SeqTuplesWriter
import trackingdataanalysis.visualization.HeatMap
import visualization.PlotOptions

class ParameterExploration(config: Config) extends GridSearch {

  def exploreFlowGateFunctionalFormLinear(constantBounds: (Double, Double, Int), linearBounds: (Double, Double, Int)): Unit = {

    val defaultParameters = createSimulation(config).getSetupArguments

    val constantRange: NumericRange[Double] = constantBounds._1 to constantBounds._2 by (constantBounds._2 - constantBounds._1) / constantBounds._3
    val linearRange: NumericRange[Double] = {linearBounds._1 to linearBounds._2 by (linearBounds._2 - linearBounds._1) / linearBounds._3}

    // checks if the output dir exists
    val outputDir = new File(config.getString("output.dir"))
    if (!outputDir.exists || !outputDir.isDirectory) {
      throw new IllegalArgumentException("Output dir for files does not exist ! dir=" + config.getString("output.dir"))
    }


    for (i <- constantRange.par; j <- linearRange.par; k <- (1 to config.getInt("sim.nb_runs")).par) {
      //Vector.fill(config.getInt("sim.nb_runs"))({

      val newDevices: ControlDevices = new ControlDevices(
        defaultParameters._11.monitoredAreas.map(_.clone()),
        defaultParameters._11.amws.map(_.clone()),
        if (config.getBoolean("sim.use_flow_gates")) {
          defaultParameters._11.flowGates.map(fg => new FlowGateFunctional(fg.startVertex, fg.endVertex, fg.start, fg.end, fg.monitoredArea, { x: Double => math.max(0.0000001, i + j * x) }))
        } else {
          Vector()
        },
        defaultParameters._11.binaryGates.map(_.clone()),
        defaultParameters._11.flowSeparators.map(_.clone())
      )

      val sim = new SFGraphSimulator(
        defaultParameters._1,
        defaultParameters._2,
        Some(config.getString("output.log_dir")),
        defaultParameters._3,
        defaultParameters._4,
        defaultParameters._5,
        defaultParameters._6,
        defaultParameters._7.clone(newDevices),
        defaultParameters._8,
        defaultParameters._9,
        defaultParameters._10,
        newDevices
      )

      runAndWriteResults(sim, i.toString + "_" + j.toString + "_params_", config.getString("output.dir"))
      System.gc()
    }
  }

    // set up the parallelism level
    //sims.tasksupport = new ForkJoinTaskSupport(new java.util.concurrent.ForkJoinPool(config.getInt("execution.threads")))


    // runs the simulations and writes the travel times to individual files
    //sims.foreach(sim => (sim._1, sim._2, runAndWriteResults(sim._3, sim._1.toString + "_" + sim._2.toString + "_params_", config.getString("output.dir"))))

    def processWrittenResults: Map[(Double, Double), ((Int, Double, Double, Double, Double, Double), (Int, Double, Double, Double, Double, Double))] = {

      val outputDir = new File(config.getString("output.dir"))
      if (!outputDir.exists || !outputDir.isDirectory) {
        throw new IllegalArgumentException("Output dir for files does not exist ! dir=" + config.getString("output.dir"))
      }

      // reads the files and process the data
      val files: Map[String, List[File]] = outputDir.listFiles.filter(_.isFile).toList.groupBy(f => {
        f.getName match {
          case a if a.contains("_params_tt_") => "tt"
          case b if b.contains("_params_density_") => "density"
        }
      })

      val ttResults: Map[(Double, Double), (Int, Double, Double, Double, Double, Double)] = files("tt").map(ProcessTTFile).
        flatMap(tup => tup._3.map(t => (tup._1, tup._2, t._1._1, t._1._2, t._2))).
        groupBy(tup => (tup._1, tup._2)).
        mapValues(v => v.flatMap(_._5).stats)

      println(ttResults)

      val densityResults: Map[(Double, Double), (Int, Double, Double, Double, Double, Double)] = files("density").map(f => {
        val endParams: Int = f.getName.indexOf("_params_density_")
        val params = f.getName.substring(0, endParams).split("_").map(_.toDouble).toVector
        val in = scala.io.Source.fromFile(f)
        val densities: Iterable[Iterable[Double]] = (for (line <- in.getLines) yield {
          val cols = line.split(",").map(_.trim)
          cols.map(_.toDouble).toVector
        }).toVector
        in.close
        (params(0), params(1), densities)//.map(ds => ds.filter(_ > 0.0)))
      }).groupBy(tup => (tup._1, tup._2)).map(tup => tup._1 -> tup._2.head._3.head.size match {
        case a if a._2 == 1 => tup._1 -> tup._2.flatMap(_._3.flatten).filter(_ > 0.0).stats
        case _ => throw new NotImplementedError("multiple density zones for parameter exploration not implemented !")
      })

      for (ttRes <- ttResults/*.filterKeys(k => (k._3, k._4) == OD2)*/.map( kv => (kv._1._1, kv._1._2) -> kv._2)) yield {
        densityResults.find(_._1 == ttRes._1) match {
          case Some(dRes) => ttRes._1 -> (ttRes._2, dRes._2)
          case None => ttRes._1 -> (ttRes._2, (0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN))
        }
      }
  }

  def processWrittenResultsSplitOD: Map[(Double, Double, String, String),((Int, Double, Double, Double, Double, Double))] = {

    val outputDir = new File(config.getString("output.dir"))

    val files: Map[String, List[File]] = outputDir.listFiles.filter(_.isFile).toList.groupBy(f => {
      f.getName match {
        case a if a.contains("_params_tt_") => "tt"
        case b if b.contains("_params_density_") => "density"
      }
    })

    files("tt").map(ProcessTTFile).
      flatMap(tup => tup._3.map(t => (tup._1, tup._2, t._1._1, t._1._2, t._2))).
      groupBy(tup => (tup._1, tup._2, tup._3, tup._4)).
      mapValues(v => v.flatMap(_._5).stats)
  }

  def drawResults(results: Map[(Double, Double), ((Int, Double, Double, Double, Double, Double), (Int, Double, Double, Double, Double, Double))]):  Unit = {

    results.map(r => (r._1._1, r._1._2, r._2._1._1, r._2._1._2, r._2._1._3, r._2._1._4, r._2._1._5, r._2._1._6)).toVector.writeToCSV(config.getString("output.output_prefix") + "_exploration-results-travel-time.csv")
    results.map(r => (r._1._1, r._1._2, r._2._2._1, r._2._2._2, r._2._2._3, r._2._2._4, r._2._2._5, r._2._2._6)).toVector.writeToCSV(config.getString("output.output_prefix") + "_exploration-results-density.csv")

    val plotOptionsTT = PlotOptions()

    new HeatMap(config.getString("output.output_prefix") + "_heatmap-mean-tt.png", results.map(r => (r._1._1, r._1._2, r._2._1._2)), "mean travel time", "constant", "linear", "mean travel time", plotOptionsTT)
    new HeatMap(config.getString("output.output_prefix") + "_heatmap-variance-tt.png", results.map(r => (r._1._1, r._1._2, r._2._1._3)), "var travel time", "constant", "linear", "variance of travel time")
    new HeatMap(config.getString("output.output_prefix") + "_heatmap-median-tt.png", results.map(r => (r._1._1, r._1._2, r._2._1._4)), "median travel time", "constant", "linear", "median of travel time", plotOptionsTT)


    new HeatMap(config.getString("output.output_prefix") + "_heatmap-mean-density.png", results.map(r => (r._1._1, r._1._2, r._2._2._2)), "mean density", "constant", "linear", "mean of density")
    new HeatMap(config.getString("output.output_prefix") + "_heatmap-variance-density.png", results.map(r => (r._1._1, r._1._2, r._2._2._3)), "var density", "constant", "linear", "variance of density")
    new HeatMap(config.getString("output.output_prefix") + "_heatmap-median-density.png", results.map(r => (r._1._1, r._1._2, r._2._2._4)), "median density", "constant", "linear", "median of density")

  }

  def drawResultsSplitOD(results: Map[(Double, Double, String, String), (Int, Double, Double, Double, Double, Double)]): Unit = {


    val OD1: (String, String) = ("left","right")
    val OD2: (String, String) = ("top","bottom")

    val plotOptionsTT = PlotOptions()

    new HeatMap(config.getString("output.output_prefix") + "_heatmap-mean-tt-" + OD1.toString() + ".png", results.filter(tup => (tup._1._3, tup._1._4) == OD1).map(r => (r._1._1, r._1._2, r._2._2)), "mean travel time", "constant", "linear", "Mean travel time from " + OD1.toString(), plotOptionsTT)
    new HeatMap(config.getString("output.output_prefix") + "_heatmap-variance-tt-" + OD1.toString() + ".png", results.filter(tup => (tup._1._3, tup._1._4) == OD1).map(r => (r._1._1, r._1._2, r._2._3)), "var travel time", "constant", "linear", "Variance travel time from " + OD1.toString())
    new HeatMap(config.getString("output.output_prefix") + "_heatmap-median-tt-" + OD1.toString() + ".png", results.filter(tup => (tup._1._3, tup._1._4) == OD1).map(r => (r._1._1, r._1._2, r._2._4)), "median travel time", "constant", "linear", "Median travel time from " + OD1.toString(), plotOptionsTT)

    new HeatMap(config.getString("output.output_prefix") + "_heatmap-mean-tt-" + OD2.toString() + ".png", results.filter(tup => (tup._1._3, tup._1._4) == OD2).map(r => (r._1._1, r._1._2, r._2._2)), "mean travel time", "constant", "linear", "Mean travel time from " + OD2.toString(), plotOptionsTT)
    new HeatMap(config.getString("output.output_prefix") + "_heatmap-variance-tt-" + OD2.toString() + ".png", results.filter(tup => (tup._1._3, tup._1._4) == OD2).map(r => (r._1._1, r._1._2, r._2._3)), "var travel time", "constant", "linear", "Variance travel time from " + OD2.toString())
    new HeatMap(config.getString("output.output_prefix") + "_heatmap-median-tt-" + OD2.toString() + ".png", results.filter(tup => (tup._1._3, tup._1._4) == OD2).map(r => (r._1._1, r._1._2, r._2._4)), "median travel time", "constant", "linear", "Median travel time from " + OD2.toString(), plotOptionsTT)


  }

}
