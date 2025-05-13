import requests
import xml.etree.ElementTree as ET
from urllib.parse import quote

bucket_name = "tfgevasv"
url = f"https://{bucket_name}.s3.amazonaws.com/?list-type=2"

response = requests.get(url)
if response.status_code != 200:
    print(f"Error al acceder al bucket: {response.status_code}")
    exit()

root = ET.fromstring(response.content)

namespace = {"s3": "http://s3.amazonaws.com/doc/2006-03-01/"}
urls = []

for content in root.findall("s3:Contents", namespace):
    key = content.find("s3:Key", namespace).text
    key_escaped = quote(key, safe="/")
    file_url = f"https://{bucket_name}.s3.amazonaws.com/{key_escaped}"
    urls.append(file_url)

with open("urls.txt", "w") as f:
    f.write("\n".join(urls))

print(f"✅ {len(urls)} URLs guardadas en urls.txt")
