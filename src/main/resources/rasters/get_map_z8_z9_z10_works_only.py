import requests
import os
import math
from math import cos, pi
from PIL import Image, ImageDraw

# Constants
API_KEY = "69db31e4-5b93-48e3-ada0-4bfb2a5a6b32"
BASE_URL = "https://tiles.stadiamaps.com/static/stamen_toner.png"

# Provided bounding box
LAT_MIN, LAT_MAX = 39.130339, 40.814076
LON_MIN, LON_MAX = -105.468393, -104.377137

# Calculate the center from the bounding box
LAT, LON = (LAT_MIN + LAT_MAX) / 2, (LON_MIN + LON_MAX) / 2

ZOOM_LEVELS = 5  # Total zoom levels
START_ZOOM = 8  # First zoom level
START_SIZE = (400, 800)  # Initial size at START_ZOOM
MAX_SIZE = 3200  # Max single request size

# Output folder
output_dir = f"maps_lat{LAT:.4f}_lon{LON:.4f}"
os.makedirs(output_dir, exist_ok=True)


def mercator_projection(lat):
    """Convert latitude to Mercator Y-coordinate."""
    return math.log(math.tan(math.pi / 4 + math.radians(lat) / 2))


def inverse_mercator(merc_y):
    """Convert Mercator Y-coordinate back to latitude."""
    return math.degrees(2 * math.atan(math.exp(merc_y)) - math.pi / 2)


def get_tile_offset(zoom, tile_size, lat):
    """Calculate the correct latitude/longitude offset for a tile using Mercator projection."""
    meters_per_pixel = (cos(lat * pi / 180) * 40075017) / (256 * (2 ** zoom))
    
    degrees_per_pixel_lat = meters_per_pixel / 111320  # Approximate meters per degree
    degrees_per_pixel_lon = 360 / (256 * (2 ** zoom))  # Linear longitude spacing

    lat_offset = degrees_per_pixel_lat * tile_size[1]  # Vertical step
    lon_offset = degrees_per_pixel_lon * tile_size[0]  # Horizontal step

    return lat_offset, lon_offset


def download_tile(zoom, size, lat, lon, filename):
    """Download a single tile image from Stadia Maps."""
    url = f"{BASE_URL}?size={size[0]}x{size[1]}@2x&center={lat},{lon}&zoom={zoom}&api_key={API_KEY}"
    response = requests.get(url)

    if response.status_code == 200:
        with open(filename, "wb") as file:
            file.write(response.content)
        return filename
    else:
        print(f"Failed to download zoom {zoom} at {lat}, {lon}")
        return None


def stitch_tiles(tiles, rows, cols, output_path):
    """Stitch multiple tiles together and add neon borders."""
    if not tiles:
        return

    tile_width, tile_height = Image.open(tiles[0]).size
    stitched_image = Image.new("RGB", (tile_width * cols, tile_height * rows))

    draw = ImageDraw.Draw(stitched_image)

    for i, tile_path in enumerate(tiles):
        img = Image.open(tile_path)
        x = (i % cols) * tile_width
        y = (i // cols) * tile_height
        stitched_image.paste(img, (x, y))

        # Draw neon border for debugging
        border_color = (0, 255, 0)  # Bright green
        draw.rectangle([x, y, x + tile_width - 1, y + tile_height - 1], outline=border_color, width=3)

        # Remove temporary tile
        os.remove(tile_path)

    stitched_image.save(output_path)
    print(f"Stitched image saved: {output_path}")


# Main process
current_zoom = START_ZOOM
current_size = START_SIZE

for i in range(ZOOM_LEVELS):
    zoom = current_zoom + i
    size = (current_size[0] * (2 ** i), current_size[1] * (2 ** i))

    if size[0] <= MAX_SIZE and size[1] <= MAX_SIZE:
        # Single tile request
        filename = os.path.join(output_dir, f"stadia_map_z{zoom}_size{size[0]}_{size[1]}.png")
        download_tile(zoom, size, LAT, LON, filename)
        print(f"Downloaded: {filename}")
    else:
        # Multi-tile request
        if zoom == 11:
            tile_rows = 2
            tile_cols = 2
        elif zoom == 12:
            tile_rows = 3
            tile_cols = 3
        else:
            tile_rows = 1
            tile_cols = 1
        
        lat_offset, lon_offset = get_tile_offset(zoom, (MAX_SIZE, MAX_SIZE), LAT)

        tile_files = []
        for r in range(tile_rows):
            for c in range(tile_cols):
                # Calculate the tile center based on the provided bounds for each zoom level
                lat_step = (LAT_MAX - LAT_MIN) * (r + 0.25)
                lon_step = (LON_MAX - LON_MIN) * (c + 0.25)

                tile_lat = LAT_MIN + lat_step
                tile_lon = LON_MIN + lon_step

                tile_filename = os.path.join(output_dir, f"tile_z{zoom}_{r}_{c}.png")

                if download_tile(zoom, (MAX_SIZE, MAX_SIZE), tile_lat, tile_lon, tile_filename):
                    tile_files.append(tile_filename)

        stitched_filename = os.path.join(output_dir, f"stadia_map_z{zoom}_stitched.png")
        stitch_tiles(tile_files, tile_rows, tile_cols, stitched_filename)
