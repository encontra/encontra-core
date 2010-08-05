package pt.inevo.encontra.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.engine.SimpleEngine;
import pt.inevo.encontra.engine.Engine;
import junit.framework.TestCase;
import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.SimpleDescriptor;
import pt.inevo.encontra.descriptors.SimpleDescriptorExtractor;
import pt.inevo.encontra.engine.SimpleIndexedObjectFactory;
import pt.inevo.encontra.image.descriptors.ColorLayoutDescriptor;
import pt.inevo.encontra.index.*;
import pt.inevo.encontra.index.annotation.Indexed;
import pt.inevo.encontra.index.search.SimpleCombinedSearcher;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.query.*;
import pt.inevo.encontra.storage.*;

/**
 * Smoke test: testing the creation of a simple engine, two indexes and the
 * execution of two random queries (testing also the combination of the queries).
 * @author ricardo
 */
public class SimpleIndexTest extends TestCase {

    public class SerializableBufferedImage implements Serializable {

        public int width;
        public int height;
        public int[] pixels;
        private static final long serialVersionUID = 7526472295622776147L;

        public SerializableBufferedImage() {
            width = 0;
            height = 0;
        }

        public SerializableBufferedImage(int width, int height, int[] pixels) {
            this.width = width;
            this.height = height;
            this.pixels = pixels;
        }

        public SerializableBufferedImage(BufferedImage image) {
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
        }

        public BufferedImage getBufferedImage() {
            if (pixels == null) {
                width = 1;
                height = 1;
                pixels = new int[1];
                pixels[0] = 128;
            }
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, getWidth(), getHeight(), getPixels(), 0, getWidth());
            return image;
        }

        /**
         * @return the width
         */
        public int getWidth() {
            return width;
        }

        /**
         * @param width the width to set
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /**
         * @return the height
         */
        public int getHeight() {
            return height;
        }

        /**
         * @param height the height to set
         */
        public void setHeight(int height) {
            this.height = height;
        }

        /**
         * @return the pixels
         */
        public int[] getPixels() {
            return pixels;
        }

        /**
         * @param pixels the pixels to set
         */
        public void setPixels(int[] pixels) {
            this.pixels = pixels;
        }
    }

    public class ImageModel implements IEntity<Long> {

        private Long id;
        private String filename;
        private String description;
        private BufferedImage image;

        public ImageModel(String filename, String description, BufferedImage image) {
            this.filename = filename;
            this.description = description;
            this.image = image;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        @Indexed
        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        @Indexed
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Indexed
        public BufferedImage getImage() {
            return image;
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }

        @Override
        public String toString() {
            return "TestModel{"
                    + "id=" + id
                    + ", title='" + filename + '\''
                    + ", content='" + description + '\''
                    + '}';
        }
    }

    public static class FileUtil {

        private static boolean hasExtension(File f, String[] extensions) {
            int sz = extensions.length;
            String ext;
            String name = f.getName();
            for (int i = 0; i < sz; i++) {
                ext = (String) extensions[i];
                if (name.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        }

        public static List<File> findFilesRecursively(File directory, String[] extensions) {
            List<File> list = new ArrayList<File>();
            if (directory.isFile()) {
                if (hasExtension(directory, extensions)) {
                    list.add(directory);
                }
                return list;
            }
            addFilesRecursevely(list, directory, extensions);
            return list;
        }

        private static void addFilesRecursevely(List<File> found, File rootDir, String[] extensions) {
            if (rootDir == null) {
                return; // we do not want waste time
            }
            File[] files = rootDir.listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                File file = new File(rootDir, files[i].getName());
                if (file.isDirectory()) {
                    addFilesRecursevely(found, file, extensions);
                } else {
                    if (hasExtension(files[i], extensions)) {
                        found.add(file);
                    }
                }
            }
        }
    }

    public class ImageModelLoader {

        protected String imagesPath = "";
        protected Long idCount = 0l;

        public ImageModelLoader() {
        }

        public ImageModelLoader(String imagesPath) {
            this.imagesPath = imagesPath;
        }

        public ImageModel loadImage(File image) {

            //for now only sets the filename
            ImageModel im = new ImageModel(image.getAbsolutePath(), "", null);
            im.setId(idCount++);

            //get the description
            //TO DO - load the description from here
            im.setDescription("Description: " + image.getAbsolutePath());

            //get the bufferedimage
            try {
                BufferedImage bufImg = ImageIO.read(image);
                im.setImage(bufImg);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return im;
        }

        public List<ImageModel> getImages(String path) {
            File root = new File(path);
            String[] extensions = {"jpg", "png"};

            List<File> imageFiles = FileUtil.findFilesRecursively(root, extensions);
            List<ImageModel> images = new ArrayList<ImageModel>();

            for (File f : imageFiles) {
                images.add(loadImage(f));
            }

            return images;
        }

        public List<ImageModel> getImages() {
            return getImages(imagesPath);
        }
    }

    public static class StringDescriptor extends SimpleDescriptor {

        public StringDescriptor() {
            super();
        }

        @Override
        public String getName() {
            return "StringDescriptor";
        }

        @Override
        public double getDistance(Descriptor other) {
            return getLevenshteinDistance(getValue(), (String) other.getValue());
        }

        public int getLevenshteinDistance(String s, String t) {
            if (s == null || t == null) {
                throw new IllegalArgumentException("Strings must not be null");
            }

            int n = s.length(); // length of s
            int m = t.length(); // length of t

            if (n == 0) {
                return m;
            } else if (m == 0) {
                return n;
            }

            int p[] = new int[n + 1]; //'previous' cost array, horizontally
            int d[] = new int[n + 1]; // cost array, horizontally
            int _d[]; //placeholder to assist in swapping p and d

            // indexes into strings s and t
            int i; // iterates through s
            int j; // iterates through t

            char t_j; // jth character of t

            int cost; // cost

            for (i = 0; i <= n; i++) {
                p[i] = i;
            }

            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);
                d[0] = j;

                for (i = 1; i <= n; i++) {
                    cost = s.charAt(i - 1) == t_j ? 0 : 1;
                    // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                    d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
                }

                // copy current distance counts to 'previous row' distance counts
                _d = p;
                p = d;
                d = _d;
            }

            // our last action in the above loop was to switch d and p, so p now
            // actually has the most recent cost counts
            return p[n];
        }
    }

    public class SimpleImageSearcher<O extends IndexedObject> extends SimpleSearcher<O> {

        @Override
        public ResultSet<O> search(Query query) {
            ResultSet<IEntry> results = new ResultSet<IEntry>();
            if (supportsQueryType(query.getType())) {
                if (query.getType().equals(Query.QueryType.KNN)) {
                    KnnQuery q = (KnnQuery) query;
                    IndexedObject o = (IndexedObject) q.getQuery();
                    if (o.getValue() instanceof BufferedImage) {
                        Descriptor d = getDescriptorExtractor().extract(o);
                        results = performKnnQuery(d, q.getKnn());
                    }
                }
            }

            return getResultObjects(results);
        }
    }

    public class SimpleTextSearcher<O extends IndexedObject> extends SimpleSearcher<O> {

        @Override
        public ResultSet<O> search(Query query) {
            ResultSet<IEntry> results = new ResultSet<IEntry>();
            if (supportsQueryType(query.getType())) {
                if (query.getType().equals(Query.QueryType.KNN)) {
                    KnnQuery q = (KnnQuery) query;
                    IndexedObject o = (IndexedObject) q.getQuery();
                    if (o.getValue() instanceof String) {
                        Descriptor d = getDescriptorExtractor().extract(o);
                        results = performKnnQuery(d, q.getKnn());
                    }
                }
            }

            return getResultObjects(results);
        }
    }

    public SimpleIndexTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMain() {
        //loading a bunch of images from the hard drive
        ImageModelLoader loader = new ImageModelLoader();
        List<ImageModel> images = loader.getImages("./src/test/resources/additional_images");

        //Creating the EntityStorage for saving the objects
        EntityStorage storage = new SimpleObjectStorage(ImageModel.class);

        //Creating the descriptor extractor for strings
        DescriptorExtractor stringDescriptorExtractor = new SimpleDescriptorExtractor(StringDescriptor.class);

        //Creating the engine
        System.out.println("Creating the Retrieval Engine...");
        Engine<ImageModel> e = new SimpleEngine<ImageModel>();
        e.setObjectStorage(storage);
        e.setIndexedObjectFactory(new SimpleIndexedObjectFactory());

        //Creating the searchers for each type of the model
        SimpleCombinedSearcher searcher = new SimpleCombinedSearcher();

        //A searcher for the filename
        SimpleTextSearcher filenameSearcher = new SimpleTextSearcher();
        filenameSearcher.setDescriptorExtractor(stringDescriptorExtractor);
        filenameSearcher.setIndex(new SimpleIndex(StringDescriptor.class));

        //A searcher for the description
        SimpleTextSearcher descriptionSearcher = new SimpleTextSearcher();
        descriptionSearcher.setDescriptorExtractor(stringDescriptorExtractor);
        descriptionSearcher.setIndex(new SimpleIndex(StringDescriptor.class));

        //A searcher for the image content
        SimpleImageSearcher imageSearcher = new SimpleImageSearcher();
        imageSearcher.setDescriptorExtractor(new ColorLayoutDescriptor<IndexedObject>(){});
        imageSearcher.setIndex(new SimpleIndex(ColorLayoutDescriptor.class));

        searcher.addSearcher("filename", filenameSearcher);
        searcher.addSearcher("description", descriptionSearcher);
        searcher.addSearcher("image", imageSearcher);

        e.setSearcher(searcher);

        System.out.println("Loading some objects to the test indexes");
        for (ImageModel im : images) {
            e.insert(im);
        }

        try {
            System.out.println("Creating a knn query...");
            BufferedImage image = ImageIO.read(new File("./src/test/resources/28004.jpg"));

            Query knnQuery = new KnnQuery(new IndexedObject<Serializable, BufferedImage>(null, image), 20);

            System.out.println("Searching for elements in the engine...");
            ResultSet<ImageModel> results = e.search(knnQuery);

            System.out.println("Number of retrieved elements: " + results.size());
            for (Result<ImageModel> r : results) {
                System.out.print("Retrieved element: " + r.getResult().toString() + "\t");
                System.out.println("Similarity: " + r.getSimilarity());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
