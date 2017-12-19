import os, json, csv, sys
import itertools
import networkx as nx
from operator import itemgetter
from collections import Counter
import threading

cur_path = os.path.dirname(os.path.abspath(__file__))
entity_types = [
    "DBpedia:Person", "DBpedia:Place",
    "DBpedia:Organisation", "DBpedia:Work",
    "DBpedia:Language", "DBpedia:Animal",
    "DBpedia:Disease" #, Other type
] 

# NOT USED AT THE MOMENT (generated from Java side)
def generate_gexf(ent_list, filename): 
    GraphT = threading.Thread(target=generate_gexf_t, args=(ent_list, filename)) 
    GraphT.start() 
 
# NOT USED AT THE MOMENT (generated from Java side)
def generate_gexf_t(ent_list, filename): 
    # types = ["DBpedia:Person", "DBpedia:Place", "DBpedia:Organisation"] 
    print("start-generate_graph", filename) 
    G = nx.Graph() 
    for k, doc_ent in ent_list.items(): 
        for x,y in itertools.combinations(doc_ent, 2): 
            nodes = [] 
            for node in [x,y]: 
                if "types" in node: 
                    for t in entity_types: 
                        if t in node["types"]: 
                            nodes.append(node) 
                            G.add_node(node["concept"], type=entity_types.index(t)) 
                else: 
                    nodes.append(node) 
                    G.add_node(node["concept"], type=len(entity_types)) 
            if len(nodes) == 2: 
                if G.has_edge(nodes[0]["concept"], nodes[1]["concept"]): 
                    G[nodes[0]["concept"]][nodes[1]["concept"]]['weight'] += 1 
                else: 
                    G.add_edge(nodes[0]["concept"], nodes[1]["concept"],\ 
                                weight=1, time=k.strftime("%Y-%m-%d")) 
 
    gname = "graph/{}".format(filename) 
    nx.write_gexf(G, gname) 
    print("finish-generate_graph", filename) 
    return 

def create_graph(f, center, ntype, num, checked):
    G = nx.read_gexf(f)

    G_nodes = []
    G_links = []
    typenodes = []
    n_from = 0
    n_to = num

    # filter checked types
    for n, d in G.nodes(data=True):
        if(d["type"] > len(checked)):
            d["type"] = 7
        if(checked[d["type"]]):
            typenodes.append(n)

    #typenodes = [n for n, d in G.nodes(data=True) if checked[d["type"]]]
    
    G = G.subgraph(typenodes)

    to_keep = [k for k, d in sorted(G.degree(weight=True),key=itemgetter(1),reverse=True)]
    Subgraph = G.subgraph(to_keep[n_from:n_to])

    for n, d in Subgraph.nodes(data=True):
        node = {"name": n, "label": d["label"], "group": d["type"]+1, "ent_type": d['entity_type'], "degree": G.degree(n)}
        if d["type"] >= len(entity_types):
            node["groupname"] = "Other"
        else:
            node["groupname"] = entity_types[d["type"]].split(":")[-1],
        if n == center:
            node["group"] = 0
        if ntype > 0:
            node["fixed"] = True
        G_nodes.append(node)
    names = [n["name"] for n in G_nodes]

    for s, t, v in Subgraph.edges(data=True):
        G_links.append({"source": names.index(s), "target": names.index(t),
                        "value": v["weight"], "time": v["time"], "label": v['label'], "url": v['url']})

    # print(G_nodes)
    # print(G_links)
    datafile = "graph/network-{}-{}.json".format(n_from, n_to)
    network = { "nodes": G_nodes, "links": G_links }
    # print(os.path.join(cur_path, "static", datafile))
    with open(os.path.join(cur_path, "static", datafile), 'w') as outfile:
        json.dump(network, outfile)

    avg_degree = nx.average_degree_connectivity(G)
    mean_degree = sum(avg_degree)/len(avg_degree)

    t = [v["time"] for s, t, v in Subgraph.edges(data=True)]
    t_counter = Counter(t)
    # print(list(t_counter.items()))
    timelines = json.dumps(list(t_counter.items()))
    return datafile, mean_degree, timelines
