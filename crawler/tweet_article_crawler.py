import os, sys, re
from elasticsearch import Elasticsearch

if len(sys.argv) > 1:
	tpc_str = sys.argv[1]
else:	
	tpc_str = 'beef_ban'

if len(sys.argv) > 2:
	max_docs = int(sys.argv[2])
else:	
	max_docs = 40000

source_type = sys.argv[3]
if source_type not in ["tweet", "article"]:
	sys.stdout.write("source type must be tweet or article")

if source_type == "tweet":
	source_dir = 'title'
else:
	source_dir = 'description'

# connect to the mmkg elastcsearch server

es = Elasticsearch(["http://130.220.208.86:9200"])
topic_dict = {"beef_ban":"crl01", "gun_control":"csc02", "gay_marriage":"chr01", 
              "climate_change":"cst01", "refugee":"cbp02"}

page_size = 80

index = "mmkg-doc-%s" % topic_dict[tpc_str]
query = {"size": page_size,
         "query": {"match": {"type" : source_type} },
          "_source": [source_dir]
         }

# query for the first batch
text_file = open("../src/extractor/lib/files/" + tpc_str + "-" + source_type + "-output.txt", "w")

res = es.search(index=index, body=query, _source=True, scroll="1h")
scroll_id = res['_scroll_id']                          

for doc in res['hits']['hits']:
	d = doc['_source']
	text_file.write(re.sub(r'^https?:\/\/.*[\r\n]*', '', d[source_dir].encode('utf-8'), flags=re.MULTILINE))
	text_file.write("\n")

text_file.close()