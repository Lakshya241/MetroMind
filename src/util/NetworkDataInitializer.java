package util;

import graph.MetroGraph;
import model.MetroLine;
import model.Station;

/**
 * Pre-configures an authentic Bengaluru Namma Metro transit network
 * featuring the Purple, Green, Yellow, Pink, and Blue (Airport & ORR) lines
 * along with major multi-line interchange hubs (Majestic, MG Road, Silk Board, etc.).
 */
public class NetworkDataInitializer {

    public static MetroGraph buildDefaultNetwork() {
        MetroGraph graph = new MetroGraph();

        // =========================================================================
        // 1. PURPLE LINE (Challaghatta to Whitefield)
        // =========================================================================
        Station challaghatta    = graph.getOrCreateStation("PL01", "Challaghatta", MetroLine.PURPLE);
        Station kengeri         = graph.getOrCreateStation("PL02", "Kengeri", MetroLine.PURPLE);
        Station mysuruRoad      = graph.getOrCreateStation("PL03", "Mysuru Road", MetroLine.PURPLE);
        Station vijayanagar     = graph.getOrCreateStation("PL04", "Vijayanagar", MetroLine.PURPLE);
        Station ksrRailway      = graph.getOrCreateStation("PL05", "KSR Bengaluru Railway Station", MetroLine.PURPLE);
        Station majestic        = graph.getOrCreateStation("PL06", "Nadaprabhu Kempegowda Station, Majestic", MetroLine.PURPLE, MetroLine.GREEN);
        Station vidhanaSoudha   = graph.getOrCreateStation("PL07", "Vidhana Soudha", MetroLine.PURPLE);
        Station mgRoad          = graph.getOrCreateStation("PL08", "MG Road", MetroLine.PURPLE, MetroLine.PINK);
        Station indiranagar     = graph.getOrCreateStation("PL09", "Indiranagar", MetroLine.PURPLE);
        Station svRoad          = graph.getOrCreateStation("PL10", "Swami Vivekananda Road", MetroLine.PURPLE);
        Station baiyappanahalli = graph.getOrCreateStation("PL11", "Baiyappanahalli", MetroLine.PURPLE);
        Station krPuram         = graph.getOrCreateStation("PL12", "KR Puram", MetroLine.PURPLE, MetroLine.BLUE);
        Station garudacharpalya = graph.getOrCreateStation("PL13", "Garudacharpalya", MetroLine.PURPLE);
        Station hoodi           = graph.getOrCreateStation("PL14", "Hoodi", MetroLine.PURPLE);
        Station sathyaSaiHosp   = graph.getOrCreateStation("PL15", "Sri Sathya Sai Hospital", MetroLine.PURPLE);
        Station whitefield      = graph.getOrCreateStation("PL16", "Whitefield (Kadugodi)", MetroLine.PURPLE);

        graph.addConnection(challaghatta, kengeri, MetroLine.PURPLE, 2.1, 3.5, true);
        graph.addConnection(kengeri, mysuruRoad, MetroLine.PURPLE, 4.3, 6.0, true);
        graph.addConnection(mysuruRoad, vijayanagar, MetroLine.PURPLE, 3.2, 4.5, true);
        graph.addConnection(vijayanagar, ksrRailway, MetroLine.PURPLE, 3.0, 4.5, true);
        graph.addConnection(ksrRailway, majestic, MetroLine.PURPLE, 1.2, 2.0, true);
        graph.addConnection(majestic, vidhanaSoudha, MetroLine.PURPLE, 1.8, 3.0, true);
        graph.addConnection(vidhanaSoudha, mgRoad, MetroLine.PURPLE, 2.2, 3.5, true);
        graph.addConnection(mgRoad, indiranagar, MetroLine.PURPLE, 3.4, 5.0, true);
        graph.addConnection(indiranagar, svRoad, MetroLine.PURPLE, 1.1, 2.0, true);
        graph.addConnection(svRoad, baiyappanahalli, MetroLine.PURPLE, 1.5, 2.5, true);
        graph.addConnection(baiyappanahalli, krPuram, MetroLine.PURPLE, 3.2, 4.5, true);
        graph.addConnection(krPuram, garudacharpalya, MetroLine.PURPLE, 2.6, 4.0, true);
        graph.addConnection(garudacharpalya, hoodi, MetroLine.PURPLE, 2.4, 3.5, true);
        graph.addConnection(hoodi, sathyaSaiHosp, MetroLine.PURPLE, 2.1, 3.0, true);
        graph.addConnection(sathyaSaiHosp, whitefield, MetroLine.PURPLE, 2.5, 3.5, true);

        // =========================================================================
        // 2. GREEN LINE (Madavara to Silk Institute)
        // =========================================================================
        Station madavara        = graph.getOrCreateStation("GL01", "Madavara (BIEC)", MetroLine.GREEN);
        Station nagasandra      = graph.getOrCreateStation("GL02", "Nagasandra", MetroLine.GREEN);
        Station peenyaIndustry  = graph.getOrCreateStation("GL03", "Peenya Industry", MetroLine.GREEN);
        Station yeshwanthpur    = graph.getOrCreateStation("GL04", "Yeshwanthpur", MetroLine.GREEN, MetroLine.BLUE);
        Station sandalSoap      = graph.getOrCreateStation("GL05", "Sandal Soap Factory", MetroLine.GREEN);
        Station rajajinagar     = graph.getOrCreateStation("GL06", "Rajajinagar", MetroLine.GREEN);
        Station mantriSquare    = graph.getOrCreateStation("GL07", "Mantri Square Sampige Road", MetroLine.GREEN);
        Station chickpet        = graph.getOrCreateStation("GL08", "Chickpet", MetroLine.GREEN);
        Station krMarket        = graph.getOrCreateStation("GL09", "Krishna Rajendra Market", MetroLine.GREEN);
        Station lalbagh         = graph.getOrCreateStation("GL10", "Lalbagh", MetroLine.GREEN);
        Station southEndCircle  = graph.getOrCreateStation("GL11", "South End Circle", MetroLine.GREEN);
        Station jayanagar       = graph.getOrCreateStation("GL12", "Jayanagar", MetroLine.GREEN);
        Station rvRoad          = graph.getOrCreateStation("GL13", "Rashtreeya Vidyalaya (RV) Road", MetroLine.GREEN, MetroLine.YELLOW);
        Station banashankari    = graph.getOrCreateStation("GL14", "Banashankari", MetroLine.GREEN);
        Station jpNagar         = graph.getOrCreateStation("GL15", "JP Nagar", MetroLine.GREEN);
        Station yelachenahalli  = graph.getOrCreateStation("GL16", "Yelachenahalli", MetroLine.GREEN);
        Station silkInstitute   = graph.getOrCreateStation("GL17", "Silk Institute", MetroLine.GREEN);

        graph.addConnection(madavara, nagasandra, MetroLine.GREEN, 3.1, 4.5, true);
        graph.addConnection(nagasandra, peenyaIndustry, MetroLine.GREEN, 2.4, 3.5, true);
        graph.addConnection(peenyaIndustry, yeshwanthpur, MetroLine.GREEN, 3.5, 5.0, true);
        graph.addConnection(yeshwanthpur, sandalSoap, MetroLine.GREEN, 1.3, 2.0, true);
        graph.addConnection(sandalSoap, rajajinagar, MetroLine.GREEN, 1.7, 2.5, true);
        graph.addConnection(rajajinagar, mantriSquare, MetroLine.GREEN, 2.6, 4.0, true);
        graph.addConnection(mantriSquare, majestic, MetroLine.GREEN, 1.8, 2.5, true);
        graph.addConnection(majestic, chickpet, MetroLine.GREEN, 1.4, 2.5, true);
        graph.addConnection(chickpet, krMarket, MetroLine.GREEN, 1.2, 2.0, true);
        graph.addConnection(krMarket, lalbagh, MetroLine.GREEN, 1.6, 2.5, true);
        graph.addConnection(lalbagh, southEndCircle, MetroLine.GREEN, 1.5, 2.5, true);
        graph.addConnection(southEndCircle, jayanagar, MetroLine.GREEN, 1.2, 2.0, true);
        graph.addConnection(jayanagar, rvRoad, MetroLine.GREEN, 1.3, 2.0, true);
        graph.addConnection(rvRoad, banashankari, MetroLine.GREEN, 1.6, 2.5, true);
        graph.addConnection(banashankari, jpNagar, MetroLine.GREEN, 1.4, 2.5, true);
        graph.addConnection(jpNagar, yelachenahalli, MetroLine.GREEN, 2.2, 3.5, true);
        graph.addConnection(yelachenahalli, silkInstitute, MetroLine.GREEN, 6.5, 9.0, true);

        // =========================================================================
        // 3. YELLOW LINE (RV Road to Bommasandra / Electronics City)
        // =========================================================================
        Station ragigudda       = graph.getOrCreateStation("YL02", "Ragigudda", MetroLine.YELLOW);
        Station jayadeva        = graph.getOrCreateStation("YL03", "Jayadeva Hospital", MetroLine.YELLOW, MetroLine.PINK);
        Station btmLayout       = graph.getOrCreateStation("YL04", "BTM Layout", MetroLine.YELLOW);
        Station centralSilkBoard= graph.getOrCreateStation("YL05", "Central Silk Board", MetroLine.YELLOW, MetroLine.BLUE);
        Station bommanahalli    = graph.getOrCreateStation("YL06", "Bommanahalli", MetroLine.YELLOW);
        Station kudluGate       = graph.getOrCreateStation("YL07", "Kudlu Gate", MetroLine.YELLOW);
        Station singasandra     = graph.getOrCreateStation("YL08", "Singasandra", MetroLine.YELLOW);
        Station electronicCity  = graph.getOrCreateStation("YL09", "Electronic City", MetroLine.YELLOW);
        Station infosysKonappana= graph.getOrCreateStation("YL10", "Infosys Foundation Konappana", MetroLine.YELLOW);
        Station bommasandra     = graph.getOrCreateStation("YL11", "Bommasandra", MetroLine.YELLOW);

        graph.addConnection(rvRoad, ragigudda, MetroLine.YELLOW, 1.8, 2.5, true);
        graph.addConnection(ragigudda, jayadeva, MetroLine.YELLOW, 1.6, 2.5, true);
        graph.addConnection(jayadeva, btmLayout, MetroLine.YELLOW, 1.5, 2.5, true);
        graph.addConnection(btmLayout, centralSilkBoard, MetroLine.YELLOW, 1.7, 2.5, true);
        graph.addConnection(centralSilkBoard, bommanahalli, MetroLine.YELLOW, 1.6, 2.5, true);
        graph.addConnection(bommanahalli, kudluGate, MetroLine.YELLOW, 2.3, 3.5, true);
        graph.addConnection(kudluGate, singasandra, MetroLine.YELLOW, 1.8, 2.5, true);
        graph.addConnection(singasandra, electronicCity, MetroLine.YELLOW, 2.5, 3.5, true);
        graph.addConnection(electronicCity, infosysKonappana, MetroLine.YELLOW, 1.4, 2.0, true);
        graph.addConnection(infosysKonappana, bommasandra, MetroLine.YELLOW, 2.7, 4.0, true);

        // =========================================================================
        // 4. PINK LINE (Kalena Agrahara to Nagawara via Dairy Circle, MG Road)
        // =========================================================================
        Station kalenaAgrahara  = graph.getOrCreateStation("PK01", "Kalena Agrahara", MetroLine.PINK);
        Station iimBangalore    = graph.getOrCreateStation("PK02", "IIM Bangalore", MetroLine.PINK);
        Station jpNagar4thPhase = graph.getOrCreateStation("PK03", "JP Nagar 4th Phase", MetroLine.PINK);
        Station dairyCircle     = graph.getOrCreateStation("PK04", "Dairy Circle", MetroLine.PINK);
        Station lakkasandra     = graph.getOrCreateStation("PK05", "Lakkasandra", MetroLine.PINK);
        Station langfordTown    = graph.getOrCreateStation("PK06", "Langford Town", MetroLine.PINK);
        Station shivajiNagar    = graph.getOrCreateStation("PK07", "Shivaji Nagar", MetroLine.PINK);
        Station cantonment      = graph.getOrCreateStation("PK08", "Bengaluru Cantonment", MetroLine.PINK);
        Station tanneryRoad     = graph.getOrCreateStation("PK09", "Tannery Road", MetroLine.PINK);
        Station nagawara        = graph.getOrCreateStation("PK10", "Nagawara", MetroLine.PINK, MetroLine.BLUE);

        graph.addConnection(kalenaAgrahara, iimBangalore, MetroLine.PINK, 2.4, 3.5, true);
        graph.addConnection(iimBangalore, jpNagar4thPhase, MetroLine.PINK, 1.5, 2.5, true);
        graph.addConnection(jpNagar4thPhase, jayadeva, MetroLine.PINK, 1.6, 2.5, true);
        graph.addConnection(jayadeva, dairyCircle, MetroLine.PINK, 2.1, 3.0, true);
        graph.addConnection(dairyCircle, lakkasandra, MetroLine.PINK, 1.4, 2.0, true);
        graph.addConnection(lakkasandra, langfordTown, MetroLine.PINK, 1.5, 2.5, true);
        graph.addConnection(langfordTown, mgRoad, MetroLine.PINK, 1.9, 3.0, true);
        graph.addConnection(mgRoad, shivajiNagar, MetroLine.PINK, 1.6, 2.5, true);
        graph.addConnection(shivajiNagar, cantonment, MetroLine.PINK, 1.4, 2.0, true);
        graph.addConnection(cantonment, tanneryRoad, MetroLine.PINK, 2.2, 3.5, true);
        graph.addConnection(tanneryRoad, nagawara, MetroLine.PINK, 3.4, 5.0, true);

        // =========================================================================
        // 5. BLUE LINE (Outer Ring Road & Airport Express: Silk Board to KIAL Airport)
        // =========================================================================
        Station hsrLayout       = graph.getOrCreateStation("BL02", "HSR Layout", MetroLine.BLUE);
        Station bellandur       = graph.getOrCreateStation("BL03", "Bellandur (Ecospace)", MetroLine.BLUE);
        Station kadubeesanahalli= graph.getOrCreateStation("BL04", "Kadubeesanahalli", MetroLine.BLUE);
        Station marathahalli    = graph.getOrCreateStation("BL05", "Marathahalli", MetroLine.BLUE);
        Station doddanekundi    = graph.getOrCreateStation("BL06", "Doddanekundi", MetroLine.BLUE);
        Station kalyanNagar     = graph.getOrCreateStation("BL08", "Kalyan Nagar", MetroLine.BLUE);
        Station hebbal          = graph.getOrCreateStation("BL09", "Hebbal", MetroLine.BLUE);
        Station yelahanka       = graph.getOrCreateStation("BL10", "Yelahanka", MetroLine.BLUE);
        Station doddajala       = graph.getOrCreateStation("BL11", "Doddajala", MetroLine.BLUE);
        Station kialAirport     = graph.getOrCreateStation("BL12", "KIAL Airport Terminal", MetroLine.BLUE);

        graph.addConnection(centralSilkBoard, hsrLayout, MetroLine.BLUE, 2.2, 3.0, true);
        graph.addConnection(hsrLayout, bellandur, MetroLine.BLUE, 3.1, 4.0, true);
        graph.addConnection(bellandur, kadubeesanahalli, MetroLine.BLUE, 1.9, 2.5, true);
        graph.addConnection(kadubeesanahalli, marathahalli, MetroLine.BLUE, 2.3, 3.0, true);
        graph.addConnection(marathahalli, doddanekundi, MetroLine.BLUE, 2.0, 2.5, true);
        graph.addConnection(doddanekundi, krPuram, MetroLine.BLUE, 3.4, 4.5, true);
        graph.addConnection(krPuram, kalyanNagar, MetroLine.BLUE, 4.2, 5.5, true);
        graph.addConnection(kalyanNagar, nagawara, MetroLine.BLUE, 3.1, 4.0, true);
        graph.addConnection(nagawara, hebbal, MetroLine.BLUE, 3.8, 5.0, true);
        graph.addConnection(hebbal, yeshwanthpur, MetroLine.BLUE, 5.6, 7.5, true);
        graph.addConnection(hebbal, yelahanka, MetroLine.BLUE, 6.7, 7.5, true);
        graph.addConnection(yelahanka, doddajala, MetroLine.BLUE, 9.4, 10.0, true);
        graph.addConnection(doddajala, kialAirport, MetroLine.BLUE, 8.2, 8.5, true);

        return graph;
    }
}
