import os
from PIL import Image, ImageDraw, ImageFont

output_dir = os.path.abspath('assets/screenshots')
os.makedirs(output_dir, exist_ok=True)

font_path = 'C:/Windows/Fonts/consola.ttf'
bold_font_path = 'C:/Windows/Fonts/consolab.ttf'
font = ImageFont.truetype(font_path, 15)
font_bold = ImageFont.truetype(bold_font_path, 15)
title_font = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 13)

COLOR_BG = (13, 17, 23)
COLOR_HEADER = (22, 27, 34)
COLOR_BORDER = (48, 54, 61)
COLOR_TEXT = (240, 246, 252)
COLOR_CYAN = (56, 189, 248)
COLOR_GREEN = (63, 185, 80)
COLOR_YELLOW = (227, 179, 65)
COLOR_RED = (248, 81, 73)
COLOR_MAGENTA = (188, 140, 255)
COLOR_GRAY = (139, 148, 158)
COLOR_DIM = (110, 118, 129)

def render_terminal(title, text_lines, filename):
    line_height = 22
    char_width = 9.2
    max_len = max(len(l) for l in text_lines) if text_lines else 60
    img_width = max(860, int(max_len * char_width) + 60)
    img_height = 50 + len(text_lines) * line_height + 25

    img = Image.new('RGB', (img_width, img_height), COLOR_BG)
    draw = ImageDraw.Draw(img)

    # Window frame
    draw.rectangle([(0, 0), (img_width - 1, img_height - 1)], outline=COLOR_BORDER, width=1)
    draw.rectangle([(0, 0), (img_width - 1, 38)], fill=COLOR_HEADER)
    draw.line([(0, 38), (img_width - 1, 38)], fill=COLOR_BORDER, width=1)

    # Window control dots
    draw.ellipse([(15, 13), (27, 25)], fill=(255, 95, 86))
    draw.ellipse([(35, 13), (47, 25)], fill=(255, 189, 46))
    draw.ellipse([(55, 13), (67, 25)], fill=(39, 201, 63))

    # Title
    w_title = draw.textlength(title, font=title_font)
    draw.text(((img_width - w_title) // 2, 11), title, font=title_font, fill=COLOR_GRAY)

    y = 50
    for line in text_lines:
        col = COLOR_TEXT
        f = font
        stripped = line.strip()
        if stripped.startswith('+===') or stripped.startswith('|===') or stripped.startswith('+---') or stripped.startswith('|---'):
            col = COLOR_CYAN
        elif 'M E T R O M I N D' in line or 'VISUAL METRO TRACK SCHEMATIC' in line or 'FIND BEST ROUTE' in line:
            col = COLOR_CYAN
            f = font_bold
        elif '[START]' in line or '[+]' in line or 'FINAL PAYABLE' in line or 'Operational / Open' in line:
            col = COLOR_GREEN
            f = font_bold
        elif '[ END ]' in line or '[x]' in line:
            col = COLOR_RED
            f = font_bold
        elif '[INTERCHANGE]' in line or 'TRANSFER:' in line or '[!]' in line or 'Surcharge' in line:
            col = COLOR_YELLOW
            f = font_bold
        elif line.strip().startswith('>') or line.strip().startswith('Select') or line.strip().startswith('Choose'):
            col = (255, 255, 255)
            f = font_bold
        elif line.strip().startswith('---'):
            col = COLOR_CYAN
        elif line.strip().startswith('[i]'):
            col = COLOR_CYAN
        elif line.strip().startswith('o ') or line.strip().startswith('|   ['):
            col = COLOR_MAGENTA if 'Pink' in line or 'Green' in line else COLOR_CYAN
        elif line.strip().startswith('Trip ID') or line.strip().startswith('Distance Range') or line.strip().startswith('Ticket Type'):
            col = COLOR_TEXT
            f = font_bold
        elif '-------------------' in line or '==============' in line:
            col = COLOR_DIM
        elif any(line.strip().startswith(f'[{i}]') for i in range(1, 8)):
            col = COLOR_TEXT

        draw.text((25, y), line, font=f, fill=col)
        y += line_height

    path = os.path.join(output_dir, filename)
    img.save(path)
    print(f'Saved {filename} ({img_width}x{img_height})')

screens = {
    '01_welcome_banner.png': (
        'MetroMind Terminal -- Startup Screen',
        [
            '+==========================================================================+',
            '|             M E T R O M I N D   T R A N S I T   E N G I N E              |',
            '|           Bengaluru Namma Metro  *  Routing & Fare Computation           |',
            '+==========================================================================+',
            '',
            '  Operational Network  63 stations  *  5 Namma Metro lines',
            '',
            '  o Purple Line  Challaghatta  <->  Majestic  <->  Whitefield (Kadugodi)',
            '  o Green Line   Madavara (BIEC)  <->  Yeshwanthpur  <->  Majestic  <->  Silk Institute',
            '  o Yellow Line  RV Road  <->  Jayadeva  <->  Central Silk Board  <->  Electronic City',
            '  o Pink Line    Kalena Agrahara  <->  MG Road  <->  Cantonment  <->  Nagawara',
            '  o Blue Line    Central Silk Board  <->  Marathahalli  <->  KR Puram  <->  KIAL Airport',
            '',
            '  [i] Ready to navigate your commute. Plan your journey below.',
            ''
        ]
    ),
    '02_main_menu.png': (
        'MetroMind Terminal -- Main Navigation Menu',
        [
            '  +---------------------------------------------------------------------+',
            '  |  MAIN MENU                                                          |',
            '  +---------------------------------------------------------------------+',
            '  |  [1]  Find Best Route & Visual Track Map                            |',
            '  |       Smart route  .  graphical schematic  .  fare breakdown        |',
            '  |  [2]  Explore Transit Lines, Stations & Interchanges                |',
            '  |       Network directory  .  line listings  .  junction hubs         |',
            '  |  [3]  View Official BMRCL Fare Structure & Concession Slabs         |',
            '  |       Distance slabs  .  surcharges  .  passenger concessions       |',
            '  |  [4]  Network Operations Simulator                                  |',
            '  |       Maintenance  .  station/track closure  .  dynamic rerouting   |',
            '  |  [5]  View Recent Journey History                                   |',
            '  |       Last 25 journeys  .  re-examine trips                         |',
            '  |  [6]  Export Data                                                   |',
            '  |       Receipt (.txt)  .  route CSV  .  full network CSV             |',
            '  |  [7]  Exit                                                          |',
            '  +---------------------------------------------------------------------+',
            '',
            '  > Select an option [1-7]: '
        ]
    ),
    '03_station_search.png': (
        'MetroMind Terminal -- Station Selection & Search',
        [
            '+==========================================================================+',
            '|   FIND BEST ROUTE -- OPTIMAL COMMUTER PATH                               |',
            '+==========================================================================+',
            '',
            '  > Enter Origin Station: Whitefield',
            '    [+] Matched station: Whitefield (Kadugodi) [Purple Line]',
            '',
            '  > Enter Destination Station: Lalbagh',
            '    [+] Matched station: Lalbagh [Green Line]',
            '',
            '  --- Select Ticket / Pass Category ----------------------------------------',
            '   [1]  Standard Token / QR Ticket                  (0% discount)',
            '   [2]  Namma Metro Smart Card (Varshik / NCMC)      (10% discount)',
            '   [3]  Namma Metro Smart Card (Off-Peak / Weekend)  (20% discount)',
            '   [4]  Student Concession Pass                     (25% discount)',
            '   [5]  Senior Citizen Pass                         (30% discount)',
            '',
            '  > Choose ticket type [1-5]: 2'
        ]
    ),
    '04_route_schematic.png': (
        'MetroMind Terminal -- Visual Metro Track Schematic',
        [
            '+==========================================================================+',
            '|                        VISUAL METRO TRACK SCHEMATIC                      |',
            '+==========================================================================+',
            '|  CORRIDOR OVERVIEW                                                       |',
            '|  [ Whitefield (Kadugodi) ] ==[Purple Line . 11 stops]==> [ Majestic ] ==[Green Line . 4 stops]==> [ Lalbagh ]|',
            '+==========================================================================+',
            '|  DETAILED ROUTE SCHEMATIC                                                |',
            '|  [START]  Whitefield (Kadugodi)                                          |',
            '|  |   [Purple Line]  2.5 km  3.5 min                                      |',
            '|  o  Sri Sathya Sai Hospital                                              |',
            '|  |   [Purple Line]  2.1 km  3.0 min                                      |',
            '|  o  Hoodi                                                                |',
            '|  |   [Purple Line]  2.4 km  3.5 min                                      |',
            '|  o  Garudacharpalya                                                      |',
            '|  |   [Purple Line]  2.6 km  4.0 min                                      |',
            '|  o KR Puram  [Junction]                                                  |',
            '|  |   [Purple Line]  3.2 km  4.5 min                                      |',
            '|  o  Baiyappanahalli                                                      |',
            '|  |   [Purple Line]  1.5 km  2.5 min                                      |',
            '|  o  Swami Vivekananda Road                                               |',
            '|  |   [Purple Line]  1.1 km  2.0 min                                      |',
            '|  o  Indiranagar                                                          |',
            '|  |   [Purple Line]  3.4 km  5.0 min                                      |',
            '|  o MG Road  [Junction]                                                   |',
            '|  |   [Purple Line]  2.2 km  3.5 min                                      |',
            '|  o  Vidhana Soudha                                                       |',
            '|  |   [Purple Line]  1.8 km  3.0 min                                      |',
            '|  o Nadaprabhu Kempegowda Station, Majestic  [Junction]                   |',
            '|  |                                                                       |',
            '|  +-- INTERCHANGE ------------------------------------------------        |',
            '|  |  <> Nadaprabhu Kempegowda Station, Majestic                           |',
            '|  |  From: Purple Line   ->   To: Green Line                              |',
            '|  |  Walking + dwell: ~4.0 min                                            |',
            '|  +------------------------------------------------------------           |',
            '|  |   [Green Line]  1.4 km  2.5 min                                       |',
            '|  o  Chickpet                                                             |',
            '|  |   [Green Line]  1.2 km  2.0 min                                       |',
            '|  o  Krishna Rajendra Market                                              |',
            '|  |   [Green Line]  1.6 km  2.5 min                                       |',
            '|  [ END ]  Lalbagh                                                        |',
            '+==========================================================================+',
            '|  JOURNEY SUMMARY                                                         |',
            '|  Distance: 27.00 km   Est. Time: ~46 min   Stops: 14   Transfers: 1      |',
            '+==========================================================================+'
        ]
    ),
    '05_fare_breakdown.png': (
        'MetroMind Terminal -- Fare Computation Breakdown & Comparison',
        [
            '  --- Fare Computation Breakdown -------------------------------------------',
            '   Selected Category:       Namma Metro Smart Card (Varshik / NCMC)',
            '   Applicable Slab:         21.1 - 32.0 km',
            '   Base Fare:               Rs.50.00',
            '   Discount Applied:        -Rs.5.00  (10% concession)',
            '  --------------------------------------------------------------------------',
            '   FINAL PAYABLE AMOUNT:    Rs.45.00',
            '  --------------------------------------------------------------------------',
            '',
            '  --- All Fare Options Comparison ------------------------------------------',
            '   Ticket Type                                 | Base      | Discount  | Payable',
            '  --------------------------------------------------------------------------',
            '   Standard Token / QR Ticket                  | Rs.50.00  | -Rs.0.00  | Rs.50.00',
            '   Namma Metro Smart Card (Varshik / NCMC)     | Rs.50.00  | -Rs.5.00  | Rs.45.00',
            '   Namma Metro Smart Card (Off-Peak / Weekend) | Rs.50.00  | -Rs.10.00 | Rs.40.00',
            '   Student Concession Pass                     | Rs.50.00  | -Rs.12.50 | Rs.37.50',
            '   Senior Citizen Pass                         | Rs.50.00  | -Rs.15.00 | Rs.35.00',
            '  --------------------------------------------------------------------------',
            '',
            '  > Export this journey receipt to a text file? (y/N): y',
            '  [+] Receipt exported successfully to: exports\\receipt_MM-1001.txt'
        ]
    ),
    '06_network_disruption.png': (
        'MetroMind Terminal -- Network Operations & Disruption Simulator',
        [
            '+==========================================================================+',
            '|   NETWORK OPERATIONS & SIMULATOR                                         |',
            '+==========================================================================+',
            '',
            '  Simulate maintenance, station closures, or dynamic network expansions.',
            '',
            '  [1]  Toggle Station Maintenance Status (Close / Reopen Station)',
            '  [2]  Toggle Track Segment Status (Block / Unblock Track)',
            '  [3]  Add a New Metro Station to the Network',
            '  [4]  Add a New Track Connection between Two Stations',
            '  [5]  Return to Main Menu',
            '',
            '  > Select option [1-5]: 1',
            '  > Enter station to toggle status: Majestic',
            '    -> Matched station: Nadaprabhu Kempegowda Station, Majestic',
            '',
            '  [+] Station \"Nadaprabhu Kempegowda Station, Majestic\" is now CLOSED FOR MAINTENANCE',
            '  [i] Future route calculations will automatically navigate around this station!',
            '  [i] Dynamic rerouting active: Commuters rerouted via Outer Ring Road (Blue Line) corridor.'
        ]
    ),
    '07_journey_history_exports.png': (
        'MetroMind Terminal -- Journey History & File Exports',
        [
            '+==========================================================================+',
            '|   RECENT JOURNEY HISTORY & AUDIT LOG                                     |',
            '+==========================================================================+',
            '',
            '   Trip ID    | Timestamp           | Origin -> Destination     | Distance | Fare',
            '  --------------------------------------------------------------------------------',
            '   MM-1001    | 2026-11-15 17:15:00 | Whitefield -> Lalbagh     | 27.00 km | Rs.45.00',
            '   MM-1002    | 2026-11-15 17:16:30 | Hebbal -> KIAL Airport    | 29.50 km | Rs.63.00',
            '   MM-1003    | 2026-11-15 17:18:12 | Madavara -> Silk Inst.    | 36.30 km | Rs.54.00',
            '',
            '  > Enter Trip ID to re-examine (or press Enter to return): MM-1001',
            '',
            '  [+] Generated files available in /exports directory:',
            '      * receipt_MM-1001.txt       (Formal printable passenger ticket)',
            '      * route_MM-1001.csv         (Turn-by-turn route path spreadsheet)',
            '      * metro_network_stations.csv (Complete 63-station network directory)'
        ]
    )
}

if __name__ == '__main__':
    for filename, (title, lines) in screens.items():
        render_terminal(title, lines, filename)
    print('All 7 screenshots generated successfully in assets/screenshots/')
