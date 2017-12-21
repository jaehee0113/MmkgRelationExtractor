import os, json
import networkx as nx
from django.shortcuts import render
from .graph import create_graph, entity_types, load_connected_components, get_num_connected_components

cur_path = os.path.dirname(os.path.abspath(__file__))
def main(request):
    typelist = ["full graph"]
    numlist = [10, 20, 50, 100, 200, 250, 300, 400, 500, 600, 700, 800, 900, 1000, 5000]
    datalist = ["select graph data"]
    datadir = os.path.join(cur_path, "graph")
    for f in os.listdir(datadir):
        if f.endswith("gexf"):
            datalist.append(f)
    display_rest = False
    connected_components = None
    topnum = 50
    errormsg = ""
    component_idx = None
    ntype = None
    center = None
    selectfile = None
    datafile = None
    timelines = None
    mean_degree = 1
    label_max_length = 9

    entitytypes = [e.split(':')[-1] for e in entity_types]
    entitytypes.append("Other")
    checked = [True for i in range(len(entitytypes))]

    if "show_connected_components" in request.GET:
        selectfile = request.GET.get("venue");
        display_rest = True

        try:
            path = os.path.join(datadir, selectfile)
            if not os.path.exists(path):
                raise Exception("Error: graph file not exists")
            f = open(path)
            connected_components = get_num_connected_components(f)
        except Exception as e:
            errormsg = e
            print(errormsg)

    if "draw" in request.GET:
        selectfile = request.GET.get("venue")
        component_idx = request.GET.get("idx")
        center = request.GET.get("center")
        ntype = request.GET.get("type")
        topnum = int(request.GET.get("topnum"))
        checked = [True if request.GET.get(t) == "on" else False for t in entitytypes]
        display_rest = True

        try:
            path = os.path.join(datadir, selectfile)
            if not os.path.exists(path):
                raise Exception("Error: graph file not exists")
            f = open(path)
            #connected_components = get_num_connected_components(f)
            datafile, mean_degree, timelines = create_graph(f, center, typelist.index(ntype), topnum, checked, component_idx, label_max_length)
        except Exception as e:
            errormsg = e
            print(errormsg)

    return render(request, "egraph.html", {
                "display_rest":display_rest,
                "connected_components": connected_components,
                "error":errormsg,
                "sfile":selectfile,
                "scenter":center,
                "stype":ntype,
                "stopnum":topnum,
                "numlist":numlist,
                "typelist":typelist,
                "datalist":datalist,
                "timelines":timelines,
                "mean_degree":mean_degree,
                "entitytypes":zip(entitytypes,checked),
                "datafile":datafile
            });
