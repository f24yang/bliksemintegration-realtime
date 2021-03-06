package nl.ovapi.rid.gtfsrt;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import nl.ovapi.rid.gtfsrt.services.ARNUritInfoToGtfsRealTimeServices;
import nl.ovapi.rid.gtfsrt.services.BisonToGtfsRealtimeService;
import nl.ovapi.rid.gtfsrt.services.KV78TurboToPseudoKV6Service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeFileWriter;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.Alerts;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.TrainUpdates;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.TripUpdates;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeGuiceBindingTypes.VehiclePositions;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeServlet;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeSink;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeSource;
import org.onebusaway.guice.jsr250.LifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class BisonToGtfsRealtimeMain {
	private static final Logger _log = LoggerFactory.getLogger(BisonToGtfsRealtimeMain.class);

	private GtfsRealtimeSink _tripUpdatesSink;
	private GtfsRealtimeSink _trainUpdatesSink;
	private GtfsRealtimeSink _vehiclePositionsSink;
	private GtfsRealtimeSink _alertsSink;

	private GtfsRealtimeSource _alertsSource;
	private GtfsRealtimeSource _tripUpdatesSource;
	private GtfsRealtimeSource _trainUpdatesSource;
	private GtfsRealtimeSource _vehiclePositionsSource;
	private BisonToGtfsRealtimeService _bisonToGtfsRealtimeService;
	//private KV78TurboToPseudoKV6Service  _kv78TurboToPseudoKV6Service;
	private ARNUritInfoToGtfsRealTimeServices  _arnuToGtfsRealTimeServices;

	private LifecycleService _lifecycleService;

	private static final String ARG_PUBADDRESS = "pubAddress";
    private static final String ARG_PUBARNUADDRESS = "pubArnuAddress";
    private static final String ARG_TRIP_UPDATES_PATH = "tripUpdatesPath";
	private static final String ARG_TRIP_UPDATES_URL = "tripUpdatesUrl";
	private static final String ARG_VEHICLE_POSITIONS_PATH = "vehiclePositionsPath";
	private static final String ARG_VEHICLE_POSITIONS_URL = "vehiclePositionsUrl";
	private static final String ARG_ALERTS_PATH = "alertsPath";
	private static final String ARG_ALERTS_URL = "alertsUrl";
	private static final String ARG_TRAIN_UPDATES_PATH = "trainUpdatesPath";
	private static final String ARG_TRAIN_UPDATES_URL = "trainUpdatesUrl";


	@Inject
	public void setLifecycleService(LifecycleService lifecycleService) {
		_lifecycleService = lifecycleService;
	}

	@Inject
	public void setTripUpdatesSink(@TripUpdates GtfsRealtimeSink tripUpdatesSink) {
		_tripUpdatesSink = tripUpdatesSink;
	}
	
	@Inject
	public void setTrainUpdatesSink(@TrainUpdates GtfsRealtimeSink trainUpdatesSink) {
		_trainUpdatesSink = trainUpdatesSink;
	}

	@Inject
	public void setBisonToGtfsRealtimeService(BisonToGtfsRealtimeService bisonToGtfsRealtimeService) {
		_bisonToGtfsRealtimeService = bisonToGtfsRealtimeService;
	}
	
	/*@Inject
	public void setKV78TurboToPseudoKV6Service(KV78TurboToPseudoKV6Service kv78TurboToPseudoKV6Service) {
		_kv78TurboToPseudoKV6Service = kv78TurboToPseudoKV6Service;
	}*/

	@Inject
	public void setARnuToGtfsRealTimeServices(ARNUritInfoToGtfsRealTimeServices arnuToGtfsRealTimeServices) {
		_arnuToGtfsRealTimeServices = arnuToGtfsRealTimeServices;
	}

	@Inject
	public void setVehiclePositionsSource(@VehiclePositions	GtfsRealtimeSource vehiclePositionsSource) {
		_vehiclePositionsSource = vehiclePositionsSource;
	}

	@Inject
	public void setTripUpdatesSource(@TripUpdates GtfsRealtimeSource tripUpdatesSource) {
		_tripUpdatesSource = tripUpdatesSource;
	}
	
	@Inject
	public void setTrainUpdatesSource(@TrainUpdates GtfsRealtimeSource trainUpdatesSource) {
		_trainUpdatesSource = trainUpdatesSource;
	}
	
	@Inject
	public void setAlertsSource(@Alerts	GtfsRealtimeSource alertsSource) {
		_alertsSource = alertsSource;
	}

	@Inject
	public void setAlertsSink(@Alerts GtfsRealtimeSink alertsSink) {
		_alertsSink = alertsSink;
	}

	@Inject
	public void setVehiclePositionsSink(@VehiclePositions GtfsRealtimeSink vehiclePositionsSink) {
		_vehiclePositionsSink = vehiclePositionsSink;
	}

	public void run(String[] args) throws Exception {
		Options options = new Options();
		buildOptions(options);
		Parser parser = new GnuParser();
		CommandLine cli = parser.parse(options, args);
		Set<Module> modules = new HashSet<Module>();
		BisonToGtfsRealtimeModule.addModuleAndDependencies(modules);
		
		Injector injector = Guice.createInjector(modules);
		injector.injectMembers(this);
		_bisonToGtfsRealtimeService.setPubAdress(cli.getOptionValue(ARG_PUBADDRESS));

        if (cli.hasOption(ARG_PUBARNUADDRESS)){
            _arnuToGtfsRealTimeServices.setArnuPubAdress(cli.getOptionValue(ARG_PUBARNUADDRESS));
        }

		if (cli.hasOption(ARG_TRIP_UPDATES_URL)) {
			GtfsRealtimeServlet servlet = injector.getInstance(GtfsRealtimeServlet.class);
			servlet.setSource(_tripUpdatesSource);
			servlet.setUrl(new URL(cli.getOptionValue(ARG_TRIP_UPDATES_URL)));
		}
		if (cli.hasOption(ARG_TRIP_UPDATES_PATH)) {
			GtfsRealtimeFileWriter fileWriter = injector.getInstance(GtfsRealtimeFileWriter.class);
			fileWriter.setSource(_tripUpdatesSource);
			fileWriter.setPath(new File(cli.getOptionValue(ARG_TRIP_UPDATES_PATH)));
		}
		
		if (cli.hasOption(ARG_TRIP_UPDATES_URL)) {
			GtfsRealtimeServlet servlet = injector.getInstance(GtfsRealtimeServlet.class);
			servlet.setSource(_tripUpdatesSource);
			servlet.setUrl(new URL(cli.getOptionValue(ARG_TRIP_UPDATES_URL)));
		}
		if (cli.hasOption(ARG_TRIP_UPDATES_PATH)) {
			GtfsRealtimeFileWriter fileWriter = injector.getInstance(GtfsRealtimeFileWriter.class);
			fileWriter.setSource(_tripUpdatesSource);
			fileWriter.setPath(new File(cli.getOptionValue(ARG_TRIP_UPDATES_PATH)));
		}

		if (cli.hasOption(ARG_TRAIN_UPDATES_URL)) {
			GtfsRealtimeServlet servlet = injector.getInstance(GtfsRealtimeServlet.class);
			servlet.setSource(_trainUpdatesSource);
			servlet.setUrl(new URL(cli.getOptionValue(ARG_TRAIN_UPDATES_URL)));
		}
		if (cli.hasOption(ARG_TRAIN_UPDATES_PATH)) {
			GtfsRealtimeFileWriter fileWriter = injector.getInstance(GtfsRealtimeFileWriter.class);
			fileWriter.setSource(_trainUpdatesSource);
			fileWriter.setPath(new File(cli.getOptionValue(ARG_TRAIN_UPDATES_PATH)));
		}

		if (cli.hasOption(ARG_VEHICLE_POSITIONS_URL)) {
			GtfsRealtimeServlet servlet = injector.getInstance(GtfsRealtimeServlet.class);
			servlet.setSource(_vehiclePositionsSource);
			servlet.setUrl(new URL(cli.getOptionValue(ARG_VEHICLE_POSITIONS_URL)));
		}
		if (cli.hasOption(ARG_VEHICLE_POSITIONS_PATH)) {
			GtfsRealtimeFileWriter fileWriter = injector.getInstance(GtfsRealtimeFileWriter.class);
			fileWriter.setSource(_vehiclePositionsSource);
			fileWriter.setPath(new File(cli.getOptionValue(ARG_VEHICLE_POSITIONS_PATH)));
		}
		if (cli.hasOption(ARG_ALERTS_URL)) {
			GtfsRealtimeServlet servlet = injector.getInstance(GtfsRealtimeServlet.class);
			servlet.setSource(_alertsSource);
			servlet.setUrl(new URL(cli.getOptionValue(ARG_ALERTS_URL)));
		}
		if (cli.hasOption(ARG_ALERTS_PATH)) {
			GtfsRealtimeFileWriter fileWriter = injector.getInstance(GtfsRealtimeFileWriter.class);
			fileWriter.setSource(_alertsSource);
			fileWriter.setPath(new File(cli.getOptionValue(ARG_ALERTS_PATH)));
		}
		_lifecycleService.start();
	}

	public static void main(String[] args) throws Exception {
		BisonToGtfsRealtimeMain m = new BisonToGtfsRealtimeMain();
		try{
			m.run(args);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	protected void buildOptions(Options options) {
		Option pubAdressOption = new Option(ARG_PUBADDRESS, true, "IP address and port of ZeroMQ publisher of BISON KV{6,15,17} data, eg 'tcp://127.0.0.1:7658'");
		pubAdressOption.setRequired(true);
		options.addOption(pubAdressOption);

        Option ArnupubAdressOption = new Option(ARG_PUBARNUADDRESS, true, "IP address and port of ZeroMQ publisher with ARNU RITinfo data, eg 'tcp://127.0.0.1:7662'");
        options.addOption(ArnupubAdressOption);

        options.addOption(ARG_TRIP_UPDATES_PATH, true, "trip updates path");
		options.addOption(ARG_TRIP_UPDATES_URL, true, "trip updates url");
		options.addOption(ARG_TRAIN_UPDATES_PATH, true, "train updates path");
		options.addOption(ARG_TRAIN_UPDATES_URL, true, "tain updates url");
		options.addOption(ARG_VEHICLE_POSITIONS_PATH, true, "vehicle positions path");
		options.addOption(ARG_VEHICLE_POSITIONS_URL, true, "vehicle positions url");
		options.addOption(ARG_ALERTS_PATH, true, "alerts path");
		options.addOption(ARG_ALERTS_URL, true, "alerts url");
	}

}
