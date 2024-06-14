import os
import time
import rdflib

def read_and_sort_files(directory):
    subdirs = [os.path.join(directory, d) for d in os.listdir(directory) if os.path.isdir(os.path.join(directory, d))]
    sorted_files = []
    for subdir in subdirs:
        files = [os.path.join(subdir, f) for f in os.listdir(subdir) if f.endswith('.ttl')]
        files.sort()
        sorted_files.extend(files)
    return sorted_files

def extract_timestamp(filename):
    base_name = os.path.basename(filename)
    timestamp_str = base_name.split('_')[0]
    return int(timestamp_str)

def stream_files(files, rate=1.0):
    previous_timestamp = None
    for file in files:
        timestamp = extract_timestamp(file)
        if previous_timestamp is not None:
            sleep_time = (timestamp - previous_timestamp) / rate
            time.sleep(max(0, sleep_time))
        previous_timestamp = timestamp
        graph = rdflib.Graph()
        graph.parse(file, format='turtle')
        print(f"Streaming file: {file}")
        # Perform streaming operation with the graph
        # For example, send the graph to a server or process it further
        # print(graph.serialize(format='turtle').decode('utf-8'))

def main():
    directory = 'SequenceData'
    sorted_files = read_and_sort_files(directory)
    stream_files(sorted_files, rate=365)  # Adjust the rate as needed

if __name__ == "__main__":
    main()
