import pandas as pd
from matplotlib.ticker import ScalarFormatter
import matplotlib.pyplot as plt
import csv 

data = pd.read_csv("data/20prefix_40random.csv",encoding='shift_jis')
data.head(3)
ax = data.plot(kind = "bar")
ax.set_xticklabels(["put", "get", "getrange", "delete"], rotation = 0)
ax.yaxis.set_major_formatter(ScalarFormatter(useMathText=True))
ax.ticklabel_format(style="sci", axis="y", scilimits=(3,5))
plt.show()