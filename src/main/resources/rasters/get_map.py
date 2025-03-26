import requests
import os

# Constants
API_KEY = "69db31e4-5b93-48e3-ada0-4bfb2a5a6b32"
BASE_URL = "https://tiles.stadiamaps.com/static/stamen_toner.png"

# Provided latitude and longitude values
lats = [39.18295578, 39.9722075, 40.76145922]
lons = [-105.4342913, -104.922765, -104.4112388]

# Desired zoom level
ZOOM_LEVEL = 11
# Image size
SIZE = (3200, 6400)

# Output directory based on image size and zoom level
output_dir = f"maps_zoom{ZOOM_LEVEL}_size{SIZE[0]}x{SIZE[1]}"
os.makedirs(output_dir, exist_ok=True)

def download_map_image(lat, lon, zoom, size, filename):
    """Download a single map image from Stadia Maps."""
    url = f"{BASE_URL}?size={size[0]}x{size[1]}&center={lat},{lon}&zoom={zoom}&api_key={API_KEY}"
    response = requests.get(url)

    if response.status_code == 200:
        with open(filename, "wb") as file:
            file.write(response.content)
        return filename
    else:
        print(f"Failed to download map image at zoom {zoom} for coordinates ({lat}, {lon})")
        return None

# Main process: Download 9 map images for all combinations of latitudes and longitudes
for i, lat in enumerate(lats):
    for j, lon in enumerate(lons):
        grid_number = i * 3 + j + 1  # Calculate grid position (1-9)

        # Construct the output filename with grid number, lat, and lon
        output_filename = os.path.join(output_dir, f"{grid_number}_stadia_map_lat{lat:.4f}_lon{lon:.4f}_zoom{ZOOM_LEVEL}_size{SIZE[0]}x{SIZE[1]}.png")

        # Download map image and save it to the directory
        download_map_image(lat, lon, ZOOM_LEVEL, SIZE, output_filename)
        print(f"Map image saved at: {output_filename}")