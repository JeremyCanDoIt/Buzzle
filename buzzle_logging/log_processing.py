import sys

def main():
    logfile = open("buzzle-log.txt")
    tsArr = []
    tjArr = []

    for line in logfile:
        #skip commented out lines
        if (line.startswith('#')):
            continue

        yah = line.split(",")
        if (len(yah) != 2):
            print(f"Error: Expected 2 values, got {len(yah)}. Line: \"{line}\"", file=sys.stderr)
            continue

        # the first value is total, the second value is jdbc only
        tsArr.append(int(yah[0]))
        tjArr.append(int(yah[1]))

    ts = (sum(tsArr) / len(tsArr)) / 1000000
    tj = (sum(tjArr) / len(tjArr)) / 1000000

    print(f"TS: {ts} ms\nTJ: {tj} ms")
    return 0


if __name__ == "__main__":
    main()
