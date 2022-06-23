import pandas as pd
import math
import numpy as np
from matplotlib import pyplot

size_array = [0, 10, 20, 30, 50, 100, 200, 300, 400, 500]
def makefigure(operation):
    results_mass = []
    results_b = []
    results_rb =[ ]
    result_hm = []
    if(operation == 0):
        op = "put"
    if(operation == 1):
        op = "get"
    if(operation == 2):
        op = "getrange"
    if(operation == 3):
        op = "delete"


    for sz in size_array:
        df = pd.read_csv("data/"+str(sz)+'prefix_20random(substring_removed).csv')
        results_mass += df['Masstree'].values.tolist()
        results_b += df['B+tree'].values.tolist()
        results_rb += df['RedBlackTree'].values.tolist()
        results_hm += df['HashMap'].values.tolist()

    result_mass = []
    result_b = []
    result_rb = []
    resutt_hm = []
        
    # put
    for i in range(len(size_array)):
        result_mass.append(results_mass[i*4+operation])
        result_b.append(results_b[i*4+operation])
        result_rb.append(results_rb[i*4+operation])
    pyplot.figure()
    pyplot.title(op)
    pyplot.xlabel('key size')
    pyplot.ylabel('throughput')
    pyplot.plot(size_array, result_mass, label='masstree')
    pyplot.plot(size_array, result_b, label='B+tree')
    pyplot.plot(size_array, result_rb, label='redblacktree')
    pyplot.plot(size_array, result_hm, label='hashmap')
    pyplot.legend()
    pyplot.savefig("graphs/" + op + ".png")
    pyplot.show()
makefigure(0)
makefigure(1)
makefigure(2)
makefigure(3)