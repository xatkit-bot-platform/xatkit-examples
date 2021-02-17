import json

with open('ibm_predictions.json') as f:
  data = json.load(f)


labels = (data["results"][0]["predictions"].keys())
d = {k:[] for k in labels}

for comment in data["results"]:

	for i,(k,v) in enumerate(comment["predictions"].items()):
		d[k].append(v)
		
#print(d)
for i, (k,v) in enumerate(d.items()):
	print(k + "," + str(v))
