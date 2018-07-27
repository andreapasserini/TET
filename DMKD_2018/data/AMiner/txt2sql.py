import sys

author_file = sys.argv[1]
paper_file = sys.argv[2]
author2paper_file = sys.argv[3]

with open(author_file) as f:
    for line in f:
        values = line.strip().split()
        if (len(values) > 0 and values[0] == "#index"):
            print("INSERT INTO author VALUES(" + values[1] + ");")
            author_id = values[1]
        if (len(values) > 1 and values[0] == "#n"):
            name = ""
            for i in range(1,len(values)-1):
                name = name + values[i]
                name = name + " "
            name = name + values[len(values)-1]
            print("INSERT INTO id2author VALUES(" + str(author_id) + ",'" + name + "');")

with open(paper_file) as f:
    for line in f:
        values = line.strip().split()
        if (len(values) > 0 and values[0] == "#index"):
            idx = values[1]
            print("INSERT INTO paper VALUES(" + idx + ");")
        elif (len(values) > 0 and values[0] == "#%"):
            # paper_paper is (citing,cited)
            print("INSERT INTO paper_paper VALUES(" + idx + "," + values[1] + ");")

with open(author2paper_file) as f:
    for line in f:
        values = line.strip().split()
        print("INSERT INTO author_paper VALUES(" + values[1] + "," + values[2] + ");")
