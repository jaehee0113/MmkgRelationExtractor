import os, json
import networkx as nx
from django.shortcuts import render
from .graph import create_graph, entity_types

cur_path = os.path.dirname(os.path.abspath(__file__))
def main(request):
    typelist = ["full graph"]
    numlist = [10, 20, 50, 100, 200]
    datalist = ["select graph data"]
    datadir = os.path.join(cur_path, "graph")
    for f in os.listdir(datadir):
        if f.endswith("gexf"):
            datalist.append(f)

    topnum = 50
    errormsg = ""
    ntype = None
    center = None
    selectfile = None
    datafile = None
    timelines = None
    mean_degree = 1

    entitytypes = [e.split(':')[-1] for e in entity_types]
    entitytypes.append("Other")
    checked = [True for i in range(len(entitytypes))]

    if "draw" in request.GET:
        selectfile = request.GET.get("venue")
        center = request.GET.get("center")
        ntype = request.GET.get("type")
        topnum = int(request.GET.get("topnum"))
        checked = [True if request.GET.get(t) == "on" else False for t in entitytypes]

        try:
            path = os.path.join(datadir, selectfile)
            if not os.path.exists(path):
                raise Exception("Error: graph file not exists")
            f = open(path)
            datafile, mean_degree, timelines = create_graph(f, center, typelist.index(ntype), topnum, checked)
        except Exception as e:
            errormsg = e
            print(errormsg)

    return render(request, "egraph.html", {
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
